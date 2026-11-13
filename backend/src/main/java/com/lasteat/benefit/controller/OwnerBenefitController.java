package com.lasteat.benefit.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.benefit.dto.BenefitPublishedRequest;
import com.lasteat.benefit.dto.BenefitRequest;
import com.lasteat.benefit.dto.OwnerBenefitResponse;
import com.lasteat.benefit.service.OwnerBenefitService;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 사장님 할인 공지 (일반 할인·제휴 혜택). 모든 API 에러: STORE_NOT_FOUND, NOT_STORE_OWNER
@Tag(name = "사장님")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1/owner/stores/{storeId}/benefits")
@RequiredArgsConstructor
public class OwnerBenefitController {

	private final OwnerBenefitService ownerBenefitService;

	@Operation(summary = "내 가게 할인 공지 목록", description = "비공개까지 전부, 최근 게시 순")
	@GetMapping
	public ApiResponse<List<OwnerBenefitResponse>> getBenefits(@PathVariable Long storeId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerBenefitService.getBenefits(storeId, member.memberId()));
	}

	@Operation(summary = "할인 공지 게시", description = "게시하면 바로 공개. 에러: INVALID_INPUT")
	@PostMapping
	public ApiResponse<OwnerBenefitResponse> create(@PathVariable Long storeId,
			@Valid @RequestBody BenefitRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerBenefitService.create(storeId, member.memberId(), request));
	}

	@Operation(summary = "할인 공지 수정", description = "공개 여부와 게시 시각은 그대로. 에러: BENEFIT_NOT_FOUND, INVALID_INPUT")
	@PutMapping("/{benefitId}")
	public ApiResponse<OwnerBenefitResponse> update(@PathVariable Long storeId, @PathVariable Long benefitId,
			@Valid @RequestBody BenefitRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerBenefitService.update(storeId, benefitId, member.memberId(), request));
	}

	@Operation(summary = "할인 공지 공개·비공개 전환", description = "에러: BENEFIT_NOT_FOUND, INVALID_INPUT")
	@PatchMapping("/{benefitId}/published")
	public ApiResponse<OwnerBenefitResponse> changePublished(@PathVariable Long storeId,
			@PathVariable Long benefitId, @Valid @RequestBody BenefitPublishedRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(
				ownerBenefitService.changePublished(storeId, benefitId, member.memberId(), request.published()));
	}

	@Operation(summary = "할인 공지 삭제", description = "응답 data는 null. 에러: BENEFIT_NOT_FOUND")
	@DeleteMapping("/{benefitId}")
	public ApiResponse<Void> delete(@PathVariable Long storeId, @PathVariable Long benefitId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		ownerBenefitService.delete(storeId, benefitId, member.memberId());
		return ApiResponse.ok();
	}

}
