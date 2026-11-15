package com.lasteat.closingdeal.dto;

import com.lasteat.closingdeal.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

// GET /members/me/reservations 쿼리 파라미터
public record ReservationSearchRequest(
		@Schema(description = "예약 상태, 여러 개는 쉼표. 없으면 전체", example = "PICKED_UP,CANCELED,NO_SHOW")
		List<ReservationStatus> status,

		@Schema(defaultValue = "0")
		@Min(value = 0, message = "page는 0~10000입니다.")
		@Max(value = 10_000, message = "page는 0~10000입니다.")
		Integer page,

		@Schema(defaultValue = "20")
		@Min(value = 1, message = "size는 1~100입니다.")
		@Max(value = 100, message = "size는 1~100입니다.")
		Integer size) {

	public ReservationSearchRequest {
		status = status == null || status.isEmpty() ? List.of(ReservationStatus.values()) : List.copyOf(status);
		page = page == null ? 0 : page;
		size = size == null ? 20 : size;
	}

}
