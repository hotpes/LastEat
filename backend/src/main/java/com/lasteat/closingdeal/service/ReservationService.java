package com.lasteat.closingdeal.service;

import com.lasteat.closingdeal.dto.ReservationResponse;
import com.lasteat.closingdeal.dto.ReservationSearchRequest;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.ClosingDealStatus;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.closingdeal.repository.ClosingDealRepository;
import com.lasteat.closingdeal.repository.ReservationRepository;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.response.PageResponse;
import com.lasteat.member.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 손님 예약. 재고를 바꾸는 예약·취소는 DealWriteRetrier가 트랜잭션과 충돌 재시도를 맡는다.
@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ClosingDealRepository closingDealRepository;

	private final ReservationRepository reservationRepository;

	private final MemberRepository memberRepository;

	private final DealWriteRetrier dealWriteRetrier;

	private final Clock clock;

	public ReservationResponse reserve(Long memberId, Long dealId, int quantity) {
		return dealWriteRetrier.run(() -> reserveOnce(memberId, dealId, quantity));
	}

	public ReservationResponse cancel(Long memberId, Long reservationId) {
		return dealWriteRetrier.run(() -> cancelOnce(memberId, reservationId));
	}

	@Transactional(readOnly = true)
	public ReservationResponse getMine(Long memberId, Long reservationId) {
		return ReservationResponse.from(findMine(memberId, reservationId));
	}

	@Transactional(readOnly = true)
	public PageResponse<ReservationResponse> listMine(Long memberId, ReservationSearchRequest request) {
		return PageResponse.from(reservationRepository
				.findMine(memberId, request.status(), PageRequest.of(request.page(), request.size()))
				.map(ReservationResponse::from));
	}

	private ReservationResponse reserveOnce(Long memberId, Long dealId, int quantity) {
		ClosingDeal deal = closingDealRepository.findWithMenuAndStore(dealId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLOSING_DEAL_NOT_FOUND));
		ClosingDealStatus status = deal.statusAt(LocalDateTime.now(clock));
		if (status == ClosingDealStatus.SCHEDULED || status == ClosingDealStatus.CLOSED) {
			throw new BusinessException(ErrorCode.CLOSING_DEAL_NOT_OPEN);
		}
		if (!reservationRepository.findReservedIds(memberId, dealId).isEmpty()) {
			throw new BusinessException(ErrorCode.ALREADY_RESERVED);
		}
		if (deal.getStock() < quantity) {
			throw new BusinessException(ErrorCode.OUT_OF_STOCK);
		}
		deal.decreaseStock(quantity);
		// 재고 UPDATE를 예약 INSERT보다 먼저 보낸다. INSERT가 먼저면 외래 키 검사로 마감할인 행에 공유 잠금이 걸리고,
		// 동시에 들어온 요청끼리 서로의 UPDATE를 기다려 교착 상태가 된다.
		closingDealRepository.flush();
		Reservation reservation = reservationRepository.save(Reservation.builder()
				.member(memberRepository.getReferenceById(memberId))
				.closingDeal(deal)
				.quantity(quantity)
				.build());
		return ReservationResponse.from(reservation);
	}

	private ReservationResponse cancelOnce(Long memberId, Long reservationId) {
		Reservation reservation = findMine(memberId, reservationId);
		ClosingDeal deal = reservation.getClosingDeal();
		boolean beforePickupEnd = LocalDateTime.now(clock).isBefore(deal.getPickupEndAt());
		if (reservation.getStatus() != ReservationStatus.RESERVED || !beforePickupEnd) {
			throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELABLE);
		}
		reservation.cancel();
		deal.increaseStock(reservation.getQuantity());
		return ReservationResponse.from(reservation);
	}

	// 다른 사람의 예약은 없는 것과 같게 응답한다
	private Reservation findMine(Long memberId, Long reservationId) {
		return reservationRepository.findWithDeal(reservationId)
				.filter(reservation -> reservation.getMember().getId().equals(memberId))
				.orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
	}

}
