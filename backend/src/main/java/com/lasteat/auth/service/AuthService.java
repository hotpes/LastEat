package com.lasteat.auth.service;

import com.lasteat.auth.dto.KakaoLoginRequest;
import com.lasteat.auth.dto.LoginResponse;
import com.lasteat.auth.dto.TokenResponse;
import com.lasteat.auth.entity.RefreshToken;
import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.auth.jwt.JwtProperties;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.auth.kakao.KakaoClient;
import com.lasteat.auth.kakao.KakaoUser;
import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.auth.token.RefreshTokens;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.member.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	static final String DEFAULT_NICKNAME = "LastEat 회원";

	private static final int MAX_NICKNAME_LENGTH = 30;

	private final KakaoClient kakaoClient;

	private final MemberRepository memberRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final JwtProvider jwtProvider;

	private final JwtProperties jwtProperties;

	private final Clock clock;

	// 처음 로그인하면 손님(CONSUMER)으로 가입한다. 사장님 권한은 운영진이 바꾼다.
	public LoginResponse loginWithKakao(KakaoLoginRequest request) {
		KakaoUser kakaoUser = kakaoClient.fetchUser(request.kakaoAccessToken());
		Optional<Member> existing = memberRepository.findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUser.id());
		Member member = existing.orElseGet(() -> memberRepository.save(newMember(kakaoUser)));
		if (request.fcmToken() != null && !request.fcmToken().isBlank()) {
			member.updateFcmToken(request.fcmToken());
		}
		refreshTokenRepository.deleteExpired(member, now());
		return LoginResponse.of(issueTokens(member), existing.isEmpty(), member);
	}

	// 이전 refreshToken은 지우고 둘 다 새로 발급한다
	public TokenResponse refresh(String rawRefreshToken) {
		RefreshToken saved = refreshTokenRepository.findByTokenHash(RefreshTokens.hash(rawRefreshToken))
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
		if (saved.isExpiredAt(now())) {
			throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
		}
		Member member = saved.getMember();
		if (refreshTokenRepository.deleteByIdReturningCount(saved.getId()) == 0) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}
		return issueTokens(member);
	}

	// 이 기기의 refreshToken만 지운다. 이미 없어도 성공으로 본다.
	public void logout(String rawRefreshToken) {
		refreshTokenRepository.deleteByTokenHash(RefreshTokens.hash(rawRefreshToken));
	}

	private TokenResponse issueTokens(Member member) {
		String rawRefreshToken = RefreshTokens.generate();
		refreshTokenRepository.save(RefreshToken.builder()
				.member(member)
				.tokenHash(RefreshTokens.hash(rawRefreshToken))
				.expiresAt(now().plus(jwtProperties.refreshTokenTtl()))
				.build());
		String accessToken = jwtProvider.issueAccessToken(new AuthMember(member.getId(), member.getRole()));
		return new TokenResponse(accessToken, rawRefreshToken);
	}

	private static Member newMember(KakaoUser kakaoUser) {
		return Member.builder()
				.provider(AuthProvider.KAKAO)
				.providerId(kakaoUser.id())
				.email(kakaoUser.email())
				.nickname(nicknameOf(kakaoUser.nickname()))
				.role(MemberRole.CONSUMER)
				.build();
	}

	static String nicknameOf(String kakaoNickname) {
		if (kakaoNickname == null || kakaoNickname.isBlank()) {
			return DEFAULT_NICKNAME;
		}
		String trimmed = kakaoNickname.strip();
		if (trimmed.codePointCount(0, trimmed.length()) <= MAX_NICKNAME_LENGTH) {
			return trimmed;
		}
		return trimmed.substring(0, trimmed.offsetByCodePoints(0, MAX_NICKNAME_LENGTH));
	}

	private LocalDateTime now() {
		return LocalDateTime.now(clock);
	}

}
