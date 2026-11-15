package com.lasteat.store.dto;

import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

// GET /stores 쿼리 파라미터. 빠진 값은 명세의 기본값으로 채운다.
public record StoreSearchRequest(
		@Schema(description = "기준 위도", example = "37.4963")
		@NotNull(message = "위도가 필요합니다.")
		@DecimalMin(value = "-90", message = "위도는 -90~90입니다.")
		@DecimalMax(value = "90", message = "위도는 -90~90입니다.")
		Double lat,

		@Schema(description = "기준 경도", example = "126.9575")
		@NotNull(message = "경도가 필요합니다.")
		@DecimalMin(value = "-180", message = "경도는 -180~180입니다.")
		@DecimalMax(value = "180", message = "경도는 -180~180입니다.")
		Double lng,

		@Schema(description = "반경(m)", defaultValue = "500")
		@Min(value = 1, message = "반경은 1m 이상입니다.")
		@Max(value = MAX_RADIUS, message = "반경은 3000m 이하입니다.")
		Integer radius,

		@Schema(description = "가게 이름·카테고리 이름·메뉴 이름 검색, 2자 이상")
		@Size(min = 2, max = 30, message = "검색어는 2~30자로 입력해 주세요.")
		String keyword,

		@Schema(description = "카테고리, 여러 개는 쉼표", example = "KOREAN,CAFE")
		List<StoreCategory> category,

		@Schema(description = "지금 영업 중인 곳만", defaultValue = "false")
		Boolean openNow,

		@Schema(description = "공개 중인 할인 공지가 있는 곳만", defaultValue = "false")
		Boolean hasBenefit,

		@Schema(description = "정렬 (1차는 DISTANCE만)", defaultValue = "DISTANCE")
		StoreSort sort,

		@Schema(defaultValue = "0")
		@Min(value = 0, message = "page는 0 이상입니다.")
		Integer page,

		@Schema(defaultValue = "20")
		@Min(value = 1, message = "size는 1~100입니다.")
		@Max(value = MAX_SIZE, message = "size는 1~100입니다.")
		Integer size) {

	public static final int MAX_RADIUS = 3000;

	public static final int MAX_SIZE = 100;

	private static final int DEFAULT_RADIUS = 500;

	private static final int DEFAULT_SIZE = 20;

	public StoreSearchRequest {
		radius = radius == null ? DEFAULT_RADIUS : radius;
		keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
		category = category == null ? List.of() : List.copyOf(category);
		openNow = Boolean.TRUE.equals(openNow);
		hasBenefit = Boolean.TRUE.equals(hasBenefit);
		sort = sort == null ? StoreSort.DISTANCE : sort;
		page = page == null ? 0 : page;
		size = size == null ? DEFAULT_SIZE : size;
	}

}
