package com.lasteat.closingdeal.dto;

import com.lasteat.closingdeal.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

// GET /owner/stores/{storeId}/reservations 쿼리 파라미터. date가 없으면 서비스가 오늘로 채운다.
public record OwnerReservationSearchRequest(
		@Schema(description = "마감할인 노출 시작 날짜, 기본 오늘", example = "2026-11-16")
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate date,

		@Schema(description = "특정 마감할인만")
		Long closingDealId,

		@Schema(description = "예약 상태, 여러 개는 쉼표. 없으면 전체", example = "RESERVED")
		List<ReservationStatus> status,

		@Schema(defaultValue = "0")
		@Min(value = 0, message = "page는 0~10000입니다.")
		@Max(value = 10_000, message = "page는 0~10000입니다.")
		Integer page,

		@Schema(defaultValue = "20")
		@Min(value = 1, message = "size는 1~100입니다.")
		@Max(value = 100, message = "size는 1~100입니다.")
		Integer size) {

	public OwnerReservationSearchRequest {
		status = status == null || status.isEmpty() ? List.of(ReservationStatus.values()) : List.copyOf(status);
		page = page == null ? 0 : page;
		size = size == null ? 20 : size;
	}

}
