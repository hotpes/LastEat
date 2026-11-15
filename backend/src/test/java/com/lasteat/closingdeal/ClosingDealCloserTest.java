package com.lasteat.closingdeal;

import static org.assertj.core.api.Assertions.assertThat;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.ClosingDealStatus;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.closingdeal.service.ClosingDealCloser;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

// 수령 마감이 지난 마감할인을 마감하고, 받아 가지 않은 예약을 노쇼로 바꾼다.
@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, ClosingDealTestSupport.FixedClockConfig.class})
class ClosingDealCloserTest extends ClosingDealTestSupport {

	@Autowired
	private ClosingDealCloser closingDealCloser;

	private Menu menu;

	private Member customer;

	@BeforeEach
	void setUp() {
		cleanUp();
		customer = saveMember("customer", MemberRole.CONSUMER);
		Store store = saveStore(null, "연남 온기", StoreCategory.KOREAN, LAT, LNG);
		menu = saveMenu(store, "연어 덮밥", 15000);
	}

	@Test
	void 수령_마감이_지난_마감할인을_마감하고_30분이_더_지나면_수령_대기_예약은_노쇼가_된다() {
		// 수령 마감 31분 지남 → 마감 + 노쇼
		ClosingDeal expired = saveDeal(menu, 9000, 3, NOW.minusHours(2), NOW.minusMinutes(31));
		// 수령 마감 10분 지남 → 마감만, 예약은 아직 수령 대기 (조금 늦게 온 손님)
		ClosingDeal justEnded = saveDeal(menu, 9000, 3, NOW.minusHours(1), NOW.minusMinutes(10));
		ClosingDeal ongoing = saveDeal(menu, 9000, 3, NOW.minusHours(1), NOW.plusMinutes(30));
		Reservation waiting = reservationRepository.save(reservation(expired));
		Reservation pickedUp = reservation(expired);
		pickedUp.pickUp(NOW.minusMinutes(40));
		reservationRepository.save(pickedUp);
		Reservation late = reservationRepository.save(reservation(justEnded));
		Reservation ongoingWaiting = reservationRepository.save(reservation(ongoing));

		closingDealCloser.closeExpired();

		assertThat(statusOf(expired)).isEqualTo(ClosingDealStatus.CLOSED);
		assertThat(statusOf(justEnded)).isEqualTo(ClosingDealStatus.CLOSED);
		assertThat(statusOf(ongoing)).isEqualTo(ClosingDealStatus.SCHEDULED);
		assertThat(reservationStatusOf(waiting)).isEqualTo(ReservationStatus.NO_SHOW);
		assertThat(reservationStatusOf(pickedUp)).isEqualTo(ReservationStatus.PICKED_UP);
		assertThat(reservationStatusOf(late)).isEqualTo(ReservationStatus.RESERVED);
		assertThat(reservationStatusOf(ongoingWaiting)).isEqualTo(ReservationStatus.RESERVED);

		// 다시 돌려도 바뀌는 것이 없다
		closingDealCloser.closeExpired();
		assertThat(reservationStatusOf(late)).isEqualTo(ReservationStatus.RESERVED);
	}

	@Test
	void 사장님이_조기_마감한_마감할인도_수령_마감_30분_뒤에_노쇼_처리된다() {
		ClosingDeal closedEarly = saveDeal(menu, 9000, 3, NOW.minusHours(2), NOW.minusMinutes(31));
		closedEarly.close();
		closingDealRepository.save(closedEarly);
		Reservation waiting = reservationRepository.save(reservation(closedEarly));

		closingDealCloser.closeExpired();

		assertThat(reservationStatusOf(waiting)).isEqualTo(ReservationStatus.NO_SHOW);
	}

	private Reservation reservation(ClosingDeal deal) {
		return Reservation.builder().member(customer).closingDeal(deal).quantity(1).build();
	}

	private ClosingDealStatus statusOf(ClosingDeal deal) {
		return closingDealRepository.findById(deal.getId()).orElseThrow().getStatus();
	}

	private ReservationStatus reservationStatusOf(Reservation reservation) {
		return reservationRepository.findById(reservation.getId()).orElseThrow().getStatus();
	}

}
