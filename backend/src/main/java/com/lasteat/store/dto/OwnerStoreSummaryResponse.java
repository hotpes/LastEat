package com.lasteat.store.dto;

import com.lasteat.store.entity.BusinessStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 내 가게 목록 항목
public record OwnerStoreSummaryResponse(
		@Schema(example = "3") Long id,
		@Schema(example = "연남 온기") String name,
		@Schema(example = "서울 마포구 동교로38길 12, 1층") String address,
		@Schema(description = "대표 사진, 없으면 null", example = "https://cdn.lasteat.app/stores/3/main.jpg")
		String thumbnailUrl,
		@Schema(example = "OPEN") BusinessStatus businessStatus) {
}
