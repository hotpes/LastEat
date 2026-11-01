package com.lasteat.closingdeal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class ReservationTest {

	private static final LocalDateTime PICKED_UP_AT = LocalDateTime.of(2026, 11, 2, 20, 30);

	@Test
	void 수량이_1_미만이면_생성할_수_없다() {
		assertThatThrownBy(() -> Reservation.builder().quantity(0).build())
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 생성하면_RESERVED_상태다() {
		assertThat(reservation().getStatus()).isEqualTo(ReservationStatus.RESERVED);
	}

	@Test
	void 수령하면_PICKED_UP이_되고_수령_시각이_기록된다() {
		Reservation reservation = reservation();

		reservation.pickUp(PICKED_UP_AT);

		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PICKED_UP);
		assertThat(reservation.getPickedUpAt()).isEqualTo(PICKED_UP_AT);
	}

	@Test
	void 취소하면_CANCELED가_된다() {
		Reservation reservation = reservation();

		reservation.cancel();

		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
	}

	@Test
	void 노쇼_처리하면_NO_SHOW가_된다() {
		Reservation reservation = reservation();

		reservation.markNoShow();

		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
	}

	@Test
	void RESERVED가_아니면_상태를_바꿀_수_없다() {
		assertTransitionFailsAfterCancel(r -> r.pickUp(PICKED_UP_AT));
		assertTransitionFailsAfterCancel(Reservation::cancel);
		assertTransitionFailsAfterCancel(Reservation::markNoShow);
	}

	private static void assertTransitionFailsAfterCancel(Consumer<Reservation> transition) {
		Reservation reservation = reservation();
		reservation.cancel();

		assertThatThrownBy(() -> transition.accept(reservation)).isInstanceOf(IllegalStateException.class);
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
	}

	private static Reservation reservation() {
		return Reservation.builder().quantity(1).build();
	}

}
