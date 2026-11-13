package com.lasteat.closingdeal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
		@Schema(description = "예약 수량, 1~5", example = "2")
		@NotNull(message = "수량을 선택해 주세요.")
		@Min(value = 1, message = "수량은 1~5개입니다.")
		@Max(value = MAX_QUANTITY, message = "수량은 1~5개입니다.")
		Integer quantity) {

	public static final int MAX_QUANTITY = 5;

}
