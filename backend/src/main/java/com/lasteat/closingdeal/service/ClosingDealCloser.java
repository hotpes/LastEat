package com.lasteat.closingdeal.service;

import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.repository.ClosingDealRepository;
import com.lasteat.closingdeal.repository.ReservationRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 수령 마감이 지난 마감할인을 마감하고, 수령 마감 30분 뒤까지 받아 가지 않은 예약을 노쇼로 바꾼다.
// 조회·예약은 시각으로 직접 판단하므로 이 작업이 늦어도 손님에게 보이는 결과는 같다.
@Service
@RequiredArgsConstructor
public class ClosingDealCloser {

	// 손님이 조금 늦게 와도 사장님이 수령 완료 처리할 수 있는 시간
	public static final Duration NO_SHOW_GRACE = Duration.ofMinutes(30);

	private final ClosingDealRepository closingDealRepository;

	private final ReservationRepository reservationRepository;

	private final Clock clock;

	@Transactional
	public void closeExpired() {
		LocalDateTime now = LocalDateTime.now(clock);
		List<ClosingDeal> expired = closingDealRepository.findExpiredNotClosed(now);
		expired.forEach(ClosingDeal::close);
		// 조기 마감한 마감할인도 포함해서, 수령 마감 + 유예 시간이 지난 수령 대기 예약은 노쇼
		reservationRepository.markNoShowPickupEndedBefore(now.minus(NO_SHOW_GRACE), now);
	}

}
