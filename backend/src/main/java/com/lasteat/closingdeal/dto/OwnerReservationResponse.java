package com.lasteat.closingdeal.dto;

import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.global.time.SeoulTime;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

// 사장님 예약 목록 항목. 손님 연락처는 주지 않는다. member·closingDeal·menu가 함께 로드돼 있어야 한다.
public record OwnerReservationResponse(
		@Schema(description = "예약 번호", example = "105") Long id,
		@Schema(example = "RESERVED") ReservationStatus status,
		@Schema(example = "2") int quantity,
		@Schema(description = "가게에서 받을 금액", example = "18000") int totalPrice,
		@Schema(example = "2026-11-16T20:12:00+09:00") OffsetDateTime reservedAt,
		@Schema(description = "수령 완료 시각, 아니면 null") OffsetDateTime pickedUpAt,
		@Schema(example = "맛집탐험가") String memberNickname,
		DealInfo closingDeal) {

	public record DealInfo(
			@Schema(example = "41") Long id,
			@Schema(example = "연어 아보카도 덮밥") String menuName,
			@Schema(example = "2026-11-16T21:30:00+09:00") OffsetDateTime pickupEndAt) {
	}

	public static OwnerReservationResponse from(Reservation reservation) {
		ClosingDeal deal = reservation.getClosingDeal();
		return new OwnerReservationResponse(
				reservation.getId(),
				reservation.getStatus(),
				reservation.getQuantity(),
				deal.getSalePrice() * reservation.getQuantity(),
				SeoulTime.toOffset(reservation.getCreatedAt()),
				SeoulTime.toOffset(reservation.getPickedUpAt()),
				reservation.getMember().getNickname(),
				new DealInfo(deal.getId(), deal.getMenu().getName(), SeoulTime.toOffset(deal.getPickupEndAt())));
	}

}
