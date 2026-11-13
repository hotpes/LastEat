package com.lasteat.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// 토큰 재발급과 로그아웃에서 쓴다
public record RefreshTokenRequest(
		@Schema(description = "로그인 때 받은 refreshToken")
		@NotBlank(message = "refreshToken이 필요합니다.")
		String refreshToken) {
}
