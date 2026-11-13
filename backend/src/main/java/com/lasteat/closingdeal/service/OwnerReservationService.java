package com.lasteat.closingdeal.service;

import com.lasteat.closingdeal.dto.OwnerReservationResponse;
import com.lasteat.closingdeal.dto.OwnerReservationSearchRequest;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.closingdeal.repository.ReservationRepository;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.response.PageResponse;
import com.lasteat.store.service.OwnedStoreReader;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 사장님 예약 확인·수령 완료·노쇼 처리
@Service
@RequiredArgsConstructor
public class OwnerReservationService {

	private final ReservationRepository reservationRepository;

	private final OwnedStoreReader ownedStoreReader;

	private final DealWriteRetrier dealWriteRetrier;

	private final Clock clock;

	@Transactional(readOnly = true)
	public PageResponse<OwnerReservationResponse> list(Long storeId, Long memberId,
			OwnerReservationSearchRequest request) {
		ownedStoreReader.read(storeId, memberId);
		LocalDate day = request.date() == null ? LocalDate.now(clock) : request.date();
		return PageResponse.from(reservationRepository
				.findForStore(storeId, day.atStartOfDay(), day.plusDays(1).atStartOfDay(), request.closingDealId(),
						request.status(), PageRequest.of(request.page(), request.size()))
				.map(OwnerReservationResponse::from));
	}

	// 손님이 조금 늦게 와도 받아 갈 수 있도록 수령 마감 30분 뒤(자동 노쇼 전)까지 처리할 수 있다.
	// 손님 취소와 동시에 들어오면 Reservation @Version으로 하나만 반영된다 (늦은 쪽은 재시도 후 상태 오류).
	public OwnerReservationResponse pickUp(Long storeId, Long reservationId, Long memberId) {
		return dealWriteRetrier.run(() -> {
			Reservation reservation = findReserved(storeId, reservationId, memberId);
			reservation.pickUp(LocalDateTime.now(clock));
			return OwnerReservationResponse.from(reservation);
		});
	}

	// 노쇼한 수량은 재고로 돌린다 (마감할인이 아직 노출 중이면 다시 예약 가능)
	public OwnerReservationResponse markNoShow(Long storeId, Long reservationId, Long memberId) {
		return dealWriteRetrier.run(() -> {
			Reservation reservation = findReserved(storeId, reservationId, memberId);
			reservation.markNoShow();
			reservation.getClosingDeal().increaseStock(reservation.getQuantity());
			return OwnerReservationResponse.from(reservation);
		});
	}

	// 다른 가게의 예약은 없는 것과 같게 응답한다
	private Reservation findReserved(Long storeId, Long reservationId, Long memberId) {
		ownedStoreReader.read(storeId, memberId);
		Reservation reservation = reservationRepository.findWithDeal(reservationId)
				.filter(found -> found.getClosingDeal().getStore().getId().equals(storeId))
				.orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
		if (reservation.getStatus() != ReservationStatus.RESERVED) {
			throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
		}
		return reservation;
	}

}
