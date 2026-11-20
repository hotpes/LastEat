package com.lasteat.registration.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.registration.dto.OwnerRegistrationRequest;
import com.lasteat.registration.dto.OwnerRegistrationResponse;
import com.lasteat.registration.service.OwnerRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 로그인한 회원이면 누구나 (손님 → 사장님 전환). SecurityConfig에서 사장님 전용 규칙보다 먼저 허용한다
@Tag(name = "사장님")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequiredArgsConstructor
public class OwnerRegistrationController {

	private final OwnerRegistrationService ownerRegistrationService;

	@Operation(summary = "사장님 등록", description = "사업자등록번호·개업연월일·대표자 성명을 국세청에 진위확인하고, 계속사업자면 "
			+ "가게를 만들어 연결한 뒤 사장님 권한이 들어간 새 토큰을 준다. 영업시간은 비어 있으니 등록 뒤 영업시간 수정으로 입력한다. "
			+ "에러: INVALID_INPUT, BUSINESS_NOT_VERIFIED, BUSINESS_NOT_ACTIVE, DUPLICATE_BUSINESS_NUMBER, "
			+ "BUSINESS_VERIFICATION_UNAVAILABLE")
	@PostMapping("/api/v1/owner/registration")
	public ApiResponse<OwnerRegistrationResponse> register(@Valid @RequestBody OwnerRegistrationRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerRegistrationService.register(member.memberId(), request));
	}

}
