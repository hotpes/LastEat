package com.lasteat.closingdeal.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.closingdeal.dto.ClosingDealCreateRequest;
import com.lasteat.closingdeal.dto.OwnerClosingDealResponse;
import com.lasteat.closingdeal.dto.OwnerReservationResponse;
import com.lasteat.closingdeal.dto.OwnerReservationSearchRequest;
import com.lasteat.closingdeal.dto.StockRequest;
import com.lasteat.closingdeal.service.OwnerClosingDealService;
import com.lasteat.closingdeal.service.OwnerReservationService;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 사장님 마감할인·예약 관리. 모든 API 에러: STORE_NOT_FOUND, NOT_STORE_OWNER
@Tag(name = "사장님")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1/owner/stores/{storeId}")
@RequiredArgsConstructor
public class OwnerClosingDealController {

	private final OwnerClosingDealService ownerClosingDealService;

	private final OwnerReservationService ownerReservationService;

	@Operation(summary = "마감할인 등록", description = "정가는 메뉴 가격. 노출 시작이 지났으면 바로 노출. "
			+ "에러: MENU_NOT_FOUND, INVALID_INPUT (fields에 salePrice·pickupEndAt 등)")
	@PostMapping("/closing-deals")
	public ApiResponse<OwnerClosingDealResponse> create(@PathVariable Long storeId,
			@Valid @RequestBody ClosingDealCreateRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerClosingDealService.create(storeId, member.memberId(), request));
	}

	@Operation(summary = "마감할인 목록 (사장님)", description = "노출 시작 날짜 기준, 기본 오늘. 노출 시작 순")
	@GetMapping("/closing-deals")
	public ApiResponse<List<OwnerClosingDealResponse>> list(@PathVariable Long storeId,
			@Parameter(description = "노출 시작 날짜, 기본 오늘", example = "2026-11-16")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerClosingDealService.list(storeId, member.memberId(), date));
	}

	@Operation(summary = "마감할인 재고 수정", description = "0이면 품절. 에러: CLOSING_DEAL_NOT_FOUND, CLOSING_DEAL_NOT_OPEN")
	@PatchMapping("/closing-deals/{dealId}/stock")
	public ApiResponse<OwnerClosingDealResponse> changeStock(@PathVariable Long storeId, @PathVariable Long dealId,
			@Valid @RequestBody StockRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(
				ownerClosingDealService.changeStock(storeId, dealId, member.memberId(), request.stock()));
	}

	@Operation(summary = "마감할인 조기 마감", description = "이미 받은 예약은 그대로 수령 가능. "
			+ "에러: CLOSING_DEAL_NOT_FOUND, CLOSING_DEAL_NOT_OPEN")
	@PostMapping("/closing-deals/{dealId}/close")
	public ApiResponse<OwnerClosingDealResponse> close(@PathVariable Long storeId, @PathVariable Long dealId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerClosingDealService.close(storeId, dealId, member.memberId()));
	}

	@Operation(summary = "예약 목록 (사장님)", description = "마감할인 노출 시작 날짜 기준, 기본 오늘. 수령 대기 먼저, 그다음 예약 순")
	@GetMapping("/reservations")
	public ApiResponse<PageResponse<OwnerReservationResponse>> reservations(@PathVariable Long storeId,
			@Valid @ParameterObject OwnerReservationSearchRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerReservationService.list(storeId, member.memberId(), request));
	}

	@Operation(summary = "수령 완료", description = "에러: RESERVATION_NOT_FOUND, INVALID_RESERVATION_STATUS")
	@PostMapping("/reservations/{reservationId}/pickup")
	public ApiResponse<OwnerReservationResponse> pickUp(@PathVariable Long storeId,
			@PathVariable Long reservationId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerReservationService.pickUp(storeId, reservationId, member.memberId()));
	}

	@Operation(summary = "노쇼 처리", description = "노쇼한 수량은 재고로 돌아감. "
			+ "에러: RESERVATION_NOT_FOUND, INVALID_RESERVATION_STATUS")
	@PostMapping("/reservations/{reservationId}/no-show")
	public ApiResponse<OwnerReservationResponse> markNoShow(@PathVariable Long storeId,
			@PathVariable Long reservationId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerReservationService.markNoShow(storeId, reservationId, member.memberId()));
	}

}
