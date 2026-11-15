package com.lasteat.benefit.controller;

import com.lasteat.benefit.dto.BenefitResponse;
import com.lasteat.benefit.dto.BenefitSearchRequest;
import com.lasteat.benefit.service.BenefitQueryService;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 공개 API. 공개 중인 할인 공지만 보인다.
@Tag(name = "할인 공지")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BenefitController {

	private final BenefitQueryService benefitQueryService;

	@Operation(summary = "주변 할인 공지 목록", description = "가까운 순, 같은 가게면 최근 게시 순. "
			+ "제휴 식당 수는 partner=true의 totalElements")
	@GetMapping("/benefits")
	public ApiResponse<PageResponse<BenefitResponse>> search(@Valid @ParameterObject BenefitSearchRequest request) {
		return ApiResponse.ok(benefitQueryService.search(request));
	}

	@Operation(summary = "할인 공지 상세", description = "에러: BENEFIT_NOT_FOUND (비공개 포함)")
	@GetMapping("/benefits/{benefitId}")
	public ApiResponse<BenefitResponse> getDetail(@PathVariable Long benefitId) {
		return ApiResponse.ok(benefitQueryService.getDetail(benefitId));
	}

	@Operation(summary = "가게 할인 공지 목록", description = "공개 중인 것만 최근 게시 순. 에러: STORE_NOT_FOUND")
	@GetMapping("/stores/{storeId}/benefits")
	public ApiResponse<List<BenefitResponse>> getStoreBenefits(@PathVariable Long storeId) {
		return ApiResponse.ok(benefitQueryService.getStoreBenefits(storeId));
	}

}
