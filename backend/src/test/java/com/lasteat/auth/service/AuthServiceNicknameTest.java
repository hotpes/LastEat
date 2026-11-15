package com.lasteat.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthServiceNicknameTest {

	@Test
	void 카카오_닉네임이_없으면_기본_닉네임을_쓴다() {
		assertThat(AuthService.nicknameOf(null)).isEqualTo(AuthService.DEFAULT_NICKNAME);
		assertThat(AuthService.nicknameOf("  ")).isEqualTo(AuthService.DEFAULT_NICKNAME);
	}

	@Test
	void 닉네임은_앞뒤_공백을_빼고_30자까지만_쓴다() {
		assertThat(AuthService.nicknameOf(" 맛집탐험가 ")).isEqualTo("맛집탐험가");
		assertThat(AuthService.nicknameOf("가".repeat(31))).isEqualTo("가".repeat(30));
	}

	@Test
	void 두_칸짜리_문자가_섞여도_글자_중간에서_자르지_않는다() {
		String smile = new String(Character.toChars(0x1F600));
		String nickname = "가".repeat(29) + smile + smile;

		assertThat(AuthService.nicknameOf(nickname)).isEqualTo("가".repeat(29) + smile);
	}

}
