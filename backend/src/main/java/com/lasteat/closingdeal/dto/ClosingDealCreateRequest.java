package com.lasteat.closingdeal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

// 마감할인 등록. 정가는 등록 시점 메뉴 가격. 할인가와 시각의 상호 조건은 서비스에서 검사한다.
public record ClosingDealCreateRequest(
		@Schema(description = "이 가게의 메뉴 id", example = "21")
		@NotNull(message = "메뉴를 선택해 주세요.")
		Long menuId,

		@Schema(description = "할인가(원), 메뉴 가격보다 낮게", example = "9000")
		@NotNull(message = "할인가를 입력해 주세요.")
		@Min(value = 0, message = "할인가는 0원 이상입니다.")
		Integer salePrice,

		@Schema(description = "수량, 1~99", example = "3")
		@NotNull(message = "수량을 입력해 주세요.")
		@Min(value = 1, message = "수량은 1~99개입니다.")
		@Max(value = MAX_STOCK, message = "수량은 1~99개입니다.")
		Integer stock,

		@Schema(description = "노출 시작. 지금보다 이르면 바로 노출", example = "2026-11-16T20:00:00+09:00")
		@NotNull(message = "노출 시작 시각을 입력해 주세요.")
		OffsetDateTime openAt,

		@Schema(description = "수령 마감. 노출 시작 이후이면서 지금 이후, 노출 시작부터 24시간 이내",
				example = "2026-11-16T21:30:00+09:00")
		@NotNull(message = "수령 마감 시각을 입력해 주세요.")
		OffsetDateTime pickupEndAt) {

	public static final int MAX_STOCK = 99;

}
