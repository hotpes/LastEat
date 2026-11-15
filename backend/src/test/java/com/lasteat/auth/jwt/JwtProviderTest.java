package com.lasteat.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.entity.MemberRole;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

	private static final String SECRET = "test-only-secret-at-least-32-bytes-long";

	private static final Instant ISSUED_AT = Instant.parse("2026-11-03T03:00:00Z");

	private final AuthMember member = new AuthMember(12L, MemberRole.OWNER);

	@Test
	void 발급한_토큰을_읽으면_회원_ID와_역할이_나온다() {
		JwtProvider provider = providerAt(ISSUED_AT);

		AuthMember parsed = provider.parseAccessToken(provider.issueAccessToken(member));

		assertThat(parsed).isEqualTo(member);
	}

	@Test
	void 한_시간이_지나면_만료다() {
		String token = providerAt(ISSUED_AT).issueAccessToken(member);

		assertThat(providerAt(ISSUED_AT.plus(Duration.ofMinutes(59))).parseAccessToken(token)).isEqualTo(member);
		assertError(() -> providerAt(ISSUED_AT.plus(Duration.ofHours(1))).parseAccessToken(token),
				ErrorCode.EXPIRED_TOKEN);
	}

	@Test
	void 다른_키로_서명했거나_내용을_바꾼_토큰은_잘못된_토큰이다() {
		String token = providerAt(ISSUED_AT).issueAccessToken(member);
		JwtProvider other = new JwtProvider(properties("another-secret-also-at-least-32-bytes!!"), clockAt(ISSUED_AT));
		String[] parts = token.split("\\.");
		String tampered = parts[0] + "." + parts[1] + "x." + parts[2];

		assertError(() -> other.parseAccessToken(token), ErrorCode.INVALID_TOKEN);
		assertError(() -> providerAt(ISSUED_AT).parseAccessToken(tampered), ErrorCode.INVALID_TOKEN);
	}

	@Test
	void 서명_없는_토큰이나_JWT가_아닌_문자열은_잘못된_토큰이다() {
		// 헤더 {"alg":"none"}, 내용 {"sub":"12","role":"OWNER","iss":"lasteat"}, 서명 없음
		String unsigned = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMiIsInJvbGUiOiJPV05FUiIsImlzcyI6Imxhc3RlYXQifQ.";

		assertError(() -> providerAt(ISSUED_AT).parseAccessToken(unsigned), ErrorCode.INVALID_TOKEN);
		assertError(() -> providerAt(ISSUED_AT).parseAccessToken("not-a-jwt"), ErrorCode.INVALID_TOKEN);
	}

	@Test
	void 키가_32바이트보다_짧으면_서버가_뜨지_않는다() {
		assertThatThrownBy(() -> new JwtProvider(properties("short"), clockAt(ISSUED_AT)))
				.isInstanceOf(IllegalStateException.class);
	}

	private static JwtProvider providerAt(Instant now) {
		return new JwtProvider(properties(SECRET), clockAt(now));
	}

	private static JwtProperties properties(String secret) {
		return new JwtProperties(secret, Duration.ofHours(1), Duration.ofDays(14));
	}

	private static Clock clockAt(Instant now) {
		return Clock.fixed(now, ZoneId.of("Asia/Seoul"));
	}

	private static void assertError(ThrowingCallable call, ErrorCode expected) {
		assertThatThrownBy(call)
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(expected);
	}

}
