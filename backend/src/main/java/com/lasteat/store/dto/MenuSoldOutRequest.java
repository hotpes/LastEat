package com.lasteat.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MenuSoldOutRequest(
		@Schema(description = "true면 품절, false면 판매 중", example = "true")
		@NotNull(message = "품절 여부를 선택해 주세요.")
		Boolean soldOut) {
}
