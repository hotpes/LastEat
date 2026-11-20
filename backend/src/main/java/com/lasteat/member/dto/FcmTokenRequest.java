package com.lasteat.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FcmTokenRequest(
		@Schema(description = "FCM 푸시 토큰", example = "dXk3...:APA91bH...")
		@NotBlank(message = "푸시 토큰이 비어 있습니다.")
		@Size(max = 255, message = "푸시 토큰이 너무 깁니다.")
		String fcmToken) {
}
