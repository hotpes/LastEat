package com.lasteat.closingdeal.dto;

import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.ClosingDealStatus;
import com.lasteat.global.time.SeoulTime;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

// 손님용 마감할인 (목록 항목과 상세가 같은 형식)
public record ClosingDealResponse(
		@Schema(example = "41") Long id,
		@Schema(description = "OPEN 예약 가능, SOLD_OUT 품절", example = "OPEN") ClosingDealStatus status,
		MenuInfo menu,
		@Schema(description = "정가 (등록 시점 메뉴 가격)", example = "15000") int originalPrice,
		@Schema(example = "9000") int salePrice,
		@Schema(description = "할인율(%), 내림", example = "40") int discountRate,
		@Schema(description = "지금 예약할 수 있는 남은 수량", example = "3") int stock,
		@Schema(example = "2026-11-16T20:00:00+09:00") OffsetDateTime openAt,
		@Schema(example = "2026-11-16T21:30:00+09:00") OffsetDateTime pickupEndAt,
		StoreInfo store,
		@Schema(description = "로그인한 손님이 이 마감할인을 수령 대기 중이면 예약 id, 아니면 null (상세에서만 채움)")
		Long myReservationId) {

	public record MenuInfo(
			@Schema(example = "21") Long id,
			@Schema(example = "연어 아보카도 덮밥") String name,
			@Schema(example = "https://cdn.lasteat.app/menus/21.jpg") String imageUrl,
			@Schema(example = "구운 연어와 고소한 아보카도의 조화") String description) {
	}

	// thumbnailUrl은 사진이 없으면 null, distance(m)는 기준 좌표가 없으면 null
	public record StoreInfo(
			@Schema(example = "3") Long id,
			@Schema(example = "연남 온기") String name,
			@Schema(example = "KOREAN") StoreCategory category,
			@Schema(example = "https://cdn.lasteat.app/stores/3/main.jpg") String thumbnailUrl,
			@Schema(example = "240") Integer distance,
			@Schema(example = "서울 마포구 동교로38길 12, 1층") String address,
			@Schema(example = "02-332-0428") String phone) {
	}

	public static ClosingDealResponse of(ClosingDeal deal, LocalDateTime now, String thumbnailUrl, Integer distance,
			Long myReservationId) {
		Menu menu = deal.getMenu();
		Store store = deal.getStore();
		return new ClosingDealResponse(
				deal.getId(),
				deal.statusAt(now),
				new MenuInfo(menu.getId(), menu.getName(), menu.getImageUrl(), menu.getDescription()),
				deal.getOriginalPrice(),
				deal.getSalePrice(),
				deal.discountRate(),
				deal.getStock(),
				SeoulTime.toOffset(deal.getOpenAt()),
				SeoulTime.toOffset(deal.getPickupEndAt()),
				new StoreInfo(store.getId(), store.getName(), store.getCategory(), thumbnailUrl, distance,
						store.getAddress(), store.getPhone()),
				myReservationId);
	}

}
