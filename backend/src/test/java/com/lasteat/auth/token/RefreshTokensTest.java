package com.lasteat.auth.token;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RefreshTokensTest {

	@Test
	void 만들_때마다_다른_토큰이_나온다() {
		assertThat(RefreshTokens.generate()).isNotEqualTo(RefreshTokens.generate());
	}

	@Test
	void 해시는_같은_입력에_같은_64자_16진수다() {
		String token = RefreshTokens.generate();

		assertThat(RefreshTokens.hash(token))
				.hasSize(64)
				.matches("[0-9a-f]+")
				.isEqualTo(RefreshTokens.hash(token))
				.isNotEqualTo(token);
	}

}
