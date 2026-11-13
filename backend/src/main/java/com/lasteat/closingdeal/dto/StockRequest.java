package com.lasteat.closingdeal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
		@Schema(description = "남은 재고, 0이면 품절", example = "5")
		@NotNull(message = "재고를 입력해 주세요.")
		@Min(value = 0, message = "재고는 0~99개입니다.")
		@Max(value = ClosingDealCreateRequest.MAX_STOCK, message = "재고는 0~99개입니다.")
		Integer stock) {
}
