package com.lasteat.store.dto;

import com.lasteat.store.entity.BusinessStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record BusinessStatusResponse(
		@Schema(description = "지금 손님에게 보이는 영업 상태", example = "CLOSED")
		BusinessStatus businessStatus,

		@Schema(description = "사장님이 오늘 직접 정한 영업 상태. AUTO면 null", example = "CLOSED")
		BusinessStatus businessStatusOverride) {
}
