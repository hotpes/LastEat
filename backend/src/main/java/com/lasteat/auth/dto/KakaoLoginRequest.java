package com.lasteat.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KakaoLoginRequest(
		@Schema(description = "카카오 SDK에서 받은 액세스 토큰", example = "kakao-access-token")
		@NotBlank(message = "카카오 토큰이 필요합니다.")
		String kakaoAccessToken,

		@Schema(description = "푸시 알림 토큰 (선택)", example = "fcm-token")
		@Size(max = 255, message = "푸시 토큰이 너무 깁니다.")
		String fcmToken) {
}
