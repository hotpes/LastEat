package com.lasteat.closingdeal.dto;

import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.ClosingDealStatus;
import com.lasteat.global.time.SeoulTime;
import com.lasteat.store.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

// 사장님 마감할인 목록 항목. menu가 함께 로드돼 있어야 한다.
public record OwnerClosingDealResponse(
		@Schema(example = "41") Long id,
		@Schema(example = "OPEN") ClosingDealStatus status,
		MenuInfo menu,
		@Schema(example = "15000") int originalPrice,
		@Schema(example = "9000") int salePrice,
		@Schema(example = "40") int discountRate,
		@Schema(description = "남은 재고", example = "3") int stock,
		@Schema(description = "수령 대기 수량 합계", example = "4") long reservedQuantity,
		@Schema(description = "수령 완료 수량 합계", example = "1") long pickedUpQuantity,
		@Schema(example = "2026-11-16T20:00:00+09:00") OffsetDateTime openAt,
		@Schema(example = "2026-11-16T21:30:00+09:00") OffsetDateTime pickupEndAt) {

	public record MenuInfo(
			@Schema(example = "21") Long id,
			@Schema(example = "연어 아보카도 덮밥") String name,
			@Schema(example = "https://cdn.lasteat.app/menus/21.jpg") String imageUrl) {
	}

	public static OwnerClosingDealResponse of(ClosingDeal deal, LocalDateTime now, long reservedQuantity,
			long pickedUpQuantity) {
		Menu menu = deal.getMenu();
		return new OwnerClosingDealResponse(
				deal.getId(),
				deal.statusAt(now),
				new MenuInfo(menu.getId(), menu.getName(), menu.getImageUrl()),
				deal.getOriginalPrice(),
				deal.getSalePrice(),
				deal.discountRate(),
				deal.getStock(),
				reservedQuantity,
				pickedUpQuantity,
				SeoulTime.toOffset(deal.getOpenAt()),
				SeoulTime.toOffset(deal.getPickupEndAt()));
	}

}
