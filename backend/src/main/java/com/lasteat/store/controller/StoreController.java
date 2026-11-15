package com.lasteat.store.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.PageResponse;
import com.lasteat.store.dto.MenuResponse;
import com.lasteat.store.dto.StoreDetailResponse;
import com.lasteat.store.dto.StoreSearchRequest;
import com.lasteat.store.dto.StoreSummaryResponse;
import com.lasteat.store.service.StoreQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 공개 API. 토큰을 함께 보내면 bookmarked가 로그인한 사람 기준으로 채워진다.
@Tag(name = "가게")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

	private final StoreQueryService storeQueryService;

	@Operation(summary = "주변 가게 목록·검색", description = "가까운 순. 지금 영업 중인 곳 수는 openNow=true의 totalElements")
	@GetMapping
	public ApiResponse<PageResponse<StoreSummaryResponse>> search(
			@Valid @ParameterObject StoreSearchRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(storeQueryService.search(request, memberIdOf(member)));
	}

	@Operation(summary = "가게 상세", description = "lat, lng를 보내면 distance 계산. 에러: STORE_NOT_FOUND")
	@GetMapping("/{storeId}")
	public ApiResponse<StoreDetailResponse> getDetail(
			@PathVariable Long storeId,
			@RequestParam(required = false)
			@DecimalMin(value = "-90", message = "위도는 -90~90입니다.")
			@DecimalMax(value = "90", message = "위도는 -90~90입니다.") Double lat,
			@RequestParam(required = false)
			@DecimalMin(value = "-180", message = "경도는 -180~180입니다.")
			@DecimalMax(value = "180", message = "경도는 -180~180입니다.") Double lng,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(storeQueryService.getDetail(storeId, GeoPoint.ofNullable(lat, lng), memberIdOf(member)));
	}

	@Operation(summary = "메뉴 목록", description = "대표 메뉴 먼저, 그다음 등록 순. 에러: STORE_NOT_FOUND")
	@GetMapping("/{storeId}/menus")
	public ApiResponse<List<MenuResponse>> getMenus(@PathVariable Long storeId) {
		return ApiResponse.ok(storeQueryService.getMenus(storeId));
	}

	private static Long memberIdOf(AuthMember member) {
		return member == null ? null : member.memberId();
	}

}
