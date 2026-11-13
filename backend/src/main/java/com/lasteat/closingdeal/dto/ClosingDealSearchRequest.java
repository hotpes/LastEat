package com.lasteat.closingdeal.dto;

import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// GET /closing-deals 쿼리 파라미터. 빠진 값은 명세의 기본값으로 채운다.
public record ClosingDealSearchRequest(
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

		@Schema(description = "반경(m)", defaultValue = "1000")
		@Min(value = 1, message = "반경은 1m 이상입니다.")
		@Max(value = MAX_RADIUS, message = "반경은 3000m 이하입니다.")
		Integer radius,

		@Schema(description = "카테고리, 여러 개는 쉼표", example = "KOREAN,CAFE")
		List<StoreCategory> category,

		@Schema(description = "true면 품절 제외", defaultValue = "false")
		Boolean available,

		@Schema(description = "DISTANCE 가까운 순, ENDING_SOON 수령 마감이 가까운 순", defaultValue = "DISTANCE")
		ClosingDealSort sort,

		@Schema(defaultValue = "0")
		@Min(value = 0, message = "page는 0 이상입니다.")
		Integer page,

		@Schema(defaultValue = "20")
		@Min(value = 1, message = "size는 1~100입니다.")
		@Max(value = MAX_SIZE, message = "size는 1~100입니다.")
		Integer size) {

	public static final int MAX_RADIUS = 3000;

	public static final int MAX_SIZE = 100;

	private static final int DEFAULT_RADIUS = 1000;

	private static final int DEFAULT_SIZE = 20;

	public ClosingDealSearchRequest {
		radius = radius == null ? DEFAULT_RADIUS : radius;
		category = category == null ? List.of() : List.copyOf(category);
		available = Boolean.TRUE.equals(available);
		sort = sort == null ? ClosingDealSort.DISTANCE : sort;
		page = page == null ? 0 : page;
		size = size == null ? DEFAULT_SIZE : size;
	}

}
