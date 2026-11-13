package com.lasteat.bookmark.dto;

import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

// GET /members/me/bookmarks 쿼리 파라미터
public record BookmarkSearchRequest(
		@Schema(description = "카테고리, 여러 개는 쉼표", example = "KOREAN,CAFE")
		List<StoreCategory> category,

		@Schema(description = "지금 영업 중인 곳만", defaultValue = "false")
		Boolean openNow,

		@Schema(description = "보내면 distance 계산. DISTANCE 정렬에는 필수")
		@DecimalMin(value = "-90", message = "위도는 -90~90입니다.")
		@DecimalMax(value = "90", message = "위도는 -90~90입니다.")
		Double lat,

		@DecimalMin(value = "-180", message = "경도는 -180~180입니다.")
		@DecimalMax(value = "180", message = "경도는 -180~180입니다.")
		Double lng,

		@Schema(description = "RECENT 최근 저장 순, DISTANCE 가까운 순", defaultValue = "RECENT")
		BookmarkSort sort,

		@Schema(defaultValue = "0")
		@Min(value = 0, message = "page는 0 이상입니다.")
		Integer page,

		@Schema(defaultValue = "20")
		@Min(value = 1, message = "size는 1~100입니다.")
		@Max(value = 100, message = "size는 1~100입니다.")
		Integer size) {

	private static final int DEFAULT_SIZE = 20;

	public BookmarkSearchRequest {
		category = category == null ? List.of() : List.copyOf(category);
		openNow = Boolean.TRUE.equals(openNow);
		sort = sort == null ? BookmarkSort.RECENT : sort;
		page = page == null ? 0 : page;
		size = size == null ? DEFAULT_SIZE : size;
	}

}
