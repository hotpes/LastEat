package com.lasteat.closingdeal.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.closingdeal.dto.ReservationResponse;
import com.lasteat.closingdeal.dto.ReservationSearchRequest;
import com.lasteat.closingdeal.service.ReservationService;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 내 예약. 다른 사람의 예약은 RESERVATION_NOT_FOUND
@Tag(name = "마감할인·예약")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@Operation(summary = "내 예약 목록", description = "최근 예약 순. 이용 예정 status=RESERVED, "
			+ "지난 예약 status=PICKED_UP,CANCELED,NO_SHOW")
	@GetMapping("/members/me/reservations")
	public ApiResponse<PageResponse<ReservationResponse>> listMine(
			@Valid @ParameterObject ReservationSearchRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(reservationService.listMine(member.memberId(), request));
	}

	@Operation(summary = "내 예약 상세 (수령 화면)", description = "에러: RESERVATION_NOT_FOUND")
	@GetMapping("/reservations/{reservationId}")
	public ApiResponse<ReservationResponse> getMine(@PathVariable Long reservationId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(reservationService.getMine(member.memberId(), reservationId));
	}

	@Operation(summary = "예약 취소", description = "수령 대기이고 수령 마감 전일 때만. 취소한 수량만큼 재고 복구. "
			+ "에러: RESERVATION_NOT_FOUND, RESERVATION_NOT_CANCELABLE")
	@PostMapping("/reservations/{reservationId}/cancel")
	public ApiResponse<ReservationResponse> cancel(@PathVariable Long reservationId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(reservationService.cancel(member.memberId(), reservationId));
	}

}
