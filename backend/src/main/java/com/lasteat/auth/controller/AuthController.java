package com.lasteat.auth.controller;

import com.lasteat.auth.dto.KakaoLoginRequest;
import com.lasteat.auth.dto.LoginResponse;
import com.lasteat.auth.dto.RefreshTokenRequest;
import com.lasteat.auth.dto.TokenResponse;
import com.lasteat.auth.service.AuthService;
import com.lasteat.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증·회원")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "카카오 로그인", description = "처음 로그인하면 자동 가입. 에러: KAKAO_AUTH_FAILED")
	@PostMapping("/kakao")
	public ApiResponse<LoginResponse> loginWithKakao(@Valid @RequestBody KakaoLoginRequest request) {
		return ApiResponse.ok(authService.loginWithKakao(request));
	}

	@Operation(summary = "토큰 재발급", description = "둘 다 새로 발급, 이전 refreshToken은 폐기. 에러: INVALID_TOKEN, EXPIRED_TOKEN")
	@PostMapping("/refresh")
	public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ApiResponse.ok(authService.refresh(request.refreshToken()));
	}

	@Operation(summary = "로그아웃", description = "이 기기의 refreshToken을 폐기. 이미 폐기된 토큰이어도 성공")
	@PostMapping("/logout")
	public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
		authService.logout(request.refreshToken());
		return ApiResponse.ok();
	}

}
