package com.lasteat.member.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.member.dto.FcmTokenRequest;
import com.lasteat.member.dto.MemberResponse;
import com.lasteat.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증·회원")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1/members/me")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@Operation(summary = "내 정보", description = "ownedStoreCount가 1 이상이면 마이 화면에 사장님 메뉴 표시. "
			+ "에러: INVALID_TOKEN (탈퇴한 회원)")
	@GetMapping
	public ApiResponse<MemberResponse> getMe(@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(memberService.getMe(member.memberId()));
	}

	@Operation(summary = "푸시 토큰 갱신", description = "응답 data는 null. 에러: INVALID_INPUT, INVALID_TOKEN")
	@PatchMapping("/fcm-token")
	public ApiResponse<Void> updateFcmToken(@Valid @RequestBody FcmTokenRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		memberService.updateFcmToken(member.memberId(), request.fcmToken());
		return ApiResponse.ok();
	}

	@Operation(summary = "회원 탈퇴", description = "저장한 가게와 토큰을 함께 삭제한다. 지난 예약 기록은 \"탈퇴한 회원\"으로 남는다. "
			+ "응답 data는 null. 에러: OWNER_HAS_STORE (연결된 가게가 있으면 운영진에게 연결 해제 요청)")
	@DeleteMapping
	public ApiResponse<Void> withdraw(@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		memberService.withdraw(member.memberId());
		return ApiResponse.ok();
	}

}
