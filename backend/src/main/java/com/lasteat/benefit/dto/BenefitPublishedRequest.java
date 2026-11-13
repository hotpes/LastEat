package com.lasteat.benefit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BenefitPublishedRequest(
		@Schema(description = "true면 공개, false면 비공개", example = "false")
		@NotNull(message = "공개 여부를 선택해 주세요.")
		Boolean published) {
}
