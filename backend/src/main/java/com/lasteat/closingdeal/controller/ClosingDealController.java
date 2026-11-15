package com.lasteat.closingdeal.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.closingdeal.dto.ClosingDealResponse;
import com.lasteat.closingdeal.dto.ClosingDealSearchRequest;
import com.lasteat.closingdeal.dto.ReservationRequest;
import com.lasteat.closingdeal.dto.ReservationResponse;
import com.lasteat.closingdeal.service.ClosingDealQueryService;
import com.lasteat.closingdeal.service.ReservationService;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 조회는 공개, 예약은 로그인 필요. 노출 중(예약 가능·품절)인 마감할인만 보인다.
@Tag(name = "마감할인·예약")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ClosingDealController {

	private final ClosingDealQueryService closingDealQueryService;

	private final ReservationService reservationService;

	@Operation(summary = "주변 마감할인 목록", description = "기본 가까운 순, sort=ENDING_SOON이면 수령 마감이 가까운 순")
	@GetMapping("/closing-deals")
	public ApiResponse<PageResponse<ClosingDealResponse>> search(
			@Valid @ParameterObject ClosingDealSearchRequest request) {
		return ApiResponse.ok(closingDealQueryService.search(request));
	}

	@Operation(summary = "가게 마감할인 목록", description = "노출 중인 것만 수령 마감이 가까운 순. 에러: STORE_NOT_FOUND")
	@GetMapping("/stores/{storeId}/closing-deals")
	public ApiResponse<List<ClosingDealResponse>> getStoreDeals(@PathVariable Long storeId) {
		return ApiResponse.ok(closingDealQueryService.getStoreDeals(storeId));
	}

	@Operation(summary = "마감할인 상세", description = "lat, lng를 보내면 거리 계산, 토큰을 보내면 myReservationId. "
			+ "에러: CLOSING_DEAL_NOT_FOUND (노출 전·마감 포함)")
	@GetMapping("/closing-deals/{dealId}")
	public ApiResponse<ClosingDealResponse> getDetail(
			@PathVariable Long dealId,
			@RequestParam(required = false)
			@DecimalMin(value = "-90", message = "위도는 -90~90입니다.")
			@DecimalMax(value = "90", message = "위도는 -90~90입니다.") Double lat,
			@RequestParam(required = false)
			@DecimalMin(value = "-180", message = "경도는 -180~180입니다.")
			@DecimalMax(value = "180", message = "경도는 -180~180입니다.") Double lng,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(closingDealQueryService.getDetail(dealId, GeoPoint.ofNullable(lat, lng),
				member == null ? null : member.memberId()));
	}

	@Operation(summary = "예약하기", description = "에러: CLOSING_DEAL_NOT_FOUND, CLOSING_DEAL_NOT_OPEN, OUT_OF_STOCK, "
			+ "ALREADY_RESERVED, RESERVATION_BUSY")
	@SecurityRequirement(name = OpenApiConfig.BEARER)
	@PostMapping("/closing-deals/{dealId}/reservations")
	public ApiResponse<ReservationResponse> reserve(@PathVariable Long dealId,
			@Valid @RequestBody ReservationRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(reservationService.reserve(member.memberId(), dealId, request.quantity()));
	}

}
