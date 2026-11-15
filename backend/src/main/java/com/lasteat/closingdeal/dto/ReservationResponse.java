package com.lasteat.closingdeal.dto;

import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.global.time.SeoulTime;
import com.lasteat.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

// 손님 예약 (수령할 때 가게에 보여 주는 화면). closingDeal·store가 함께 로드돼 있어야 한다.
public record ReservationResponse(
		@Schema(description = "예약 번호", example = "105") Long id,
		@Schema(example = "RESERVED") ReservationStatus status,
		@Schema(example = "2") int quantity,
		@Schema(description = "가게에서 결제할 금액 = 할인가 x 수량", example = "18000") int totalPrice,
		@Schema(example = "2026-11-16T20:12:00+09:00") OffsetDateTime reservedAt,
		@Schema(description = "수령 완료 시각, 아니면 null") OffsetDateTime pickedUpAt,
		@Schema(example = "2026-11-16T21:30:00+09:00") OffsetDateTime pickupEndAt,
		DealInfo closingDeal,
		StoreInfo store) {

	public record DealInfo(
			@Schema(example = "41") Long id,
			@Schema(example = "연어 아보카도 덮밥") String menuName,
			@Schema(example = "9000") int salePrice) {
	}

	public record StoreInfo(
			@Schema(example = "3") Long id,
			@Schema(example = "연남 온기") String name,
			@Schema(example = "서울 마포구 동교로38길 12, 1층") String address,
			@Schema(example = "02-332-0428") String phone) {
	}

	public static ReservationResponse from(Reservation reservation) {
		ClosingDeal deal = reservation.getClosingDeal();
		Store store = deal.getStore();
		return new ReservationResponse(
				reservation.getId(),
				reservation.getStatus(),
				reservation.getQuantity(),
				deal.getSalePrice() * reservation.getQuantity(),
				SeoulTime.toOffset(reservation.getCreatedAt()),
				SeoulTime.toOffset(reservation.getPickedUpAt()),
				SeoulTime.toOffset(deal.getPickupEndAt()),
				new DealInfo(deal.getId(), deal.getMenu().getName(), deal.getSalePrice()),
				new StoreInfo(store.getId(), store.getName(), store.getAddress(), store.getPhone()));
	}

}
