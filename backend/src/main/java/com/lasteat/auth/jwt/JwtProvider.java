package com.lasteat.auth.jwt;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.entity.MemberRole;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;

// accessToken(JWT, HS256) 발급과 검증. refreshToken은 JWT가 아니라 RefreshTokens가 만든 임의 문자열이다.
@Component
public class JwtProvider {

	static final String ISSUER = "lasteat";

	private static final String ROLE_CLAIM = "role";

	// HS256 키는 256비트 이상이어야 한다
	private static final int MIN_SECRET_BYTES = 32;

	private final byte[] secret;

	private final JwtProperties properties;

	private final Clock clock;

	public JwtProvider(JwtProperties properties, Clock clock) {
		this.secret = properties.secret().getBytes(StandardCharsets.UTF_8);
		if (secret.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("jwt.secret은 32바이트 이상이어야 합니다.");
		}
		this.properties = properties;
		this.clock = clock;
	}

	public String issueAccessToken(AuthMember member) {
		Instant now = clock.instant();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.issuer(ISSUER)
				.subject(member.memberId().toString())
				.claim(ROLE_CLAIM, member.role().name())
				.issueTime(Date.from(now))
				.expirationTime(Date.from(now.plus(properties.accessTokenTtl())))
				.build();
		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
		try {
			jwt.sign(new MACSigner(secret));
		} catch (JOSEException e) {
			throw new IllegalStateException("accessToken 서명에 실패했습니다.", e);
		}
		return jwt.serialize();
	}

	// 서명·발급자·형식이 틀리면 INVALID_TOKEN, 기간이 지났으면 EXPIRED_TOKEN
	public AuthMember parseAccessToken(String token) {
		JWTClaimsSet claims = verifiedClaims(token);
		Date expiresAt = claims.getExpirationTime();
		if (expiresAt == null || !ISSUER.equals(claims.getIssuer())) {
			throw invalid();
		}
		if (!clock.instant().isBefore(expiresAt.toInstant())) {
			throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
		}
		return toMember(claims);
	}

	private static AuthMember toMember(JWTClaimsSet claims) {
		try {
			String subject = claims.getSubject();
			String role = claims.getStringClaim(ROLE_CLAIM);
			if (subject == null || role == null) {
				throw invalid();
			}
			return new AuthMember(Long.valueOf(subject), MemberRole.valueOf(role));
		} catch (ParseException | IllegalArgumentException e) {
			throw invalid();
		}
	}

	private JWTClaimsSet verifiedClaims(String token) {
		try {
			SignedJWT jwt = SignedJWT.parse(token);
			if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(secret))) {
				throw invalid();
			}
			return jwt.getJWTClaimsSet();
		} catch (ParseException | JOSEException e) {
			throw invalid();
		}
	}

	private static BusinessException invalid() {
		return new BusinessException(ErrorCode.INVALID_TOKEN);
	}

}
