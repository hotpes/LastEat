package com.lasteat.registration.dto;

import com.lasteat.auth.dto.TokenResponse;
import io.swagger.v3.oas.annotations.media.Schema;

// 사장님 권한이 들어간 새 토큰을 함께 준다. 앱은 이전 토큰을 버리고 이 토큰으로 바꾼다
public record OwnerRegistrationResponse(
		@Schema(description = "새로 만든 가게 id", example = "3")
		Long storeId,

		@Schema(description = "사장님(OWNER) 권한이 들어간 accessToken")
		String accessToken,

		@Schema(description = "새 refreshToken")
		String refreshToken) {

	public static OwnerRegistrationResponse of(Long storeId, TokenResponse tokens) {
		return new OwnerRegistrationResponse(storeId, tokens.accessToken(), tokens.refreshToken());
	}

}
