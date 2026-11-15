package com.lasteat.auth.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lasteat.member.entity.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

	private static final String HASH = "a".repeat(64);

	private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 11, 17, 12, 0);

	private final Member member = Member.builder().nickname("회원").build();

	@Test
	void 만료_시각_전에는_유효하고_만료_시각부터는_만료다() {
		RefreshToken token = RefreshToken.builder().member(member).tokenHash(HASH).expiresAt(EXPIRES_AT).build();

		assertThat(token.isExpiredAt(EXPIRES_AT.minusSeconds(1))).isFalse();
		assertThat(token.isExpiredAt(EXPIRES_AT)).isTrue();
	}

	@Test
	void 해시가_64자가_아니면_생성할_수_없다() {
		assertThatThrownBy(() -> RefreshToken.builder().member(member).tokenHash("abc").expiresAt(EXPIRES_AT).build())
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 회원이나_만료_시각이_없으면_생성할_수_없다() {
		assertThatThrownBy(() -> RefreshToken.builder().tokenHash(HASH).expiresAt(EXPIRES_AT).build())
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> RefreshToken.builder().member(member).tokenHash(HASH).build())
				.isInstanceOf(NullPointerException.class);
	}

}
