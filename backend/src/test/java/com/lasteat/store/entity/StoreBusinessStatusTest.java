package com.lasteat.store.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StoreBusinessStatusTest {

	// 2026-11-03은 화요일
	private static final LocalDate TUESDAY = LocalDate.of(2026, 11, 3);

	@Test
	void 영업시간_안이면_영업_중이고_닫는_시각부터는_영업_종료다() {
		Store store = store(LocalTime.of(11, 0), LocalTime.of(21, 0), null, null, Set.of());

		assertThat(store.businessStatusAt(at(TUESDAY, 10, 59))).isEqualTo(BusinessStatus.CLOSED);
		assertThat(store.businessStatusAt(at(TUESDAY, 11, 0))).isEqualTo(BusinessStatus.OPEN);
		assertThat(store.businessStatusAt(at(TUESDAY, 20, 59))).isEqualTo(BusinessStatus.OPEN);
		assertThat(store.businessStatusAt(at(TUESDAY, 21, 0))).isEqualTo(BusinessStatus.CLOSED);
	}

	@Test
	void 쉬는_시간에는_준비_중이다() {
		Store store = store(LocalTime.of(11, 0), LocalTime.of(21, 0), LocalTime.of(15, 0), LocalTime.of(17, 0),
				Set.of());

		assertThat(store.businessStatusAt(at(TUESDAY, 15, 0))).isEqualTo(BusinessStatus.BREAK);
		assertThat(store.businessStatusAt(at(TUESDAY, 16, 59))).isEqualTo(BusinessStatus.BREAK);
		assertThat(store.businessStatusAt(at(TUESDAY, 17, 0))).isEqualTo(BusinessStatus.OPEN);
	}

	@Test
	void 정기_휴무_요일에는_영업시간이어도_영업_종료다() {
		Store store = store(LocalTime.of(11, 0), LocalTime.of(21, 0), null, null, Set.of(DayOfWeek.TUESDAY));

		assertThat(store.businessStatusAt(at(TUESDAY, 12, 0))).isEqualTo(BusinessStatus.CLOSED);
		assertThat(store.businessStatusAt(at(TUESDAY.plusDays(1), 12, 0))).isEqualTo(BusinessStatus.OPEN);
	}

	@Test
	void 자정을_넘기는_영업은_새벽까지_영업_중이다() {
		Store store = store(LocalTime.of(18, 0), LocalTime.of(2, 0), null, null, Set.of());

		assertThat(store.businessStatusAt(at(TUESDAY, 17, 59))).isEqualTo(BusinessStatus.CLOSED);
		assertThat(store.businessStatusAt(at(TUESDAY, 23, 0))).isEqualTo(BusinessStatus.OPEN);
		assertThat(store.businessStatusAt(at(TUESDAY, 1, 59))).isEqualTo(BusinessStatus.OPEN);
		assertThat(store.businessStatusAt(at(TUESDAY, 2, 0))).isEqualTo(BusinessStatus.CLOSED);
	}

	@Test
	void 새벽_시간은_전날_영업이라_전날이_휴무면_영업_종료다() {
		// 월요일 휴무: 화요일 01시는 월요일 영업분이라 닫힘, 화요일 23시는 영업
		Store store = store(LocalTime.of(18, 0), LocalTime.of(2, 0), null, null, Set.of(DayOfWeek.MONDAY));

		assertThat(store.businessStatusAt(at(TUESDAY, 1, 0))).isEqualTo(BusinessStatus.CLOSED);
		assertThat(store.businessStatusAt(at(TUESDAY, 23, 0))).isEqualTo(BusinessStatus.OPEN);
		// 수요일 01시는 화요일 영업분이라 영업
		assertThat(store.businessStatusAt(at(TUESDAY.plusDays(1), 1, 0))).isEqualTo(BusinessStatus.OPEN);
	}

	@Test
	void 여는_시각과_닫는_시각이_같으면_24시간_영업이다() {
		Store store = store(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, null, null, Set.of());

		assertThat(store.businessStatusAt(at(TUESDAY, 3, 0))).isEqualTo(BusinessStatus.OPEN);
	}

	@Test
	void 영업시간을_등록하지_않은_가게는_영업_종료로_본다() {
		Store store = store(null, null, null, null, Set.of());

		assertThat(store.businessStatusAt(at(TUESDAY, 12, 0))).isEqualTo(BusinessStatus.CLOSED);
	}

	@Test
	void 사장님이_정한_오늘_상태가_영업시간보다_우선하고_다음_날에는_풀린다() {
		Store store = store(LocalTime.of(11, 0), LocalTime.of(21, 0), null, null, Set.of());
		store.overrideBusinessStatus(BusinessStatus.CLOSED, TUESDAY);

		assertThat(store.businessStatusAt(at(TUESDAY, 12, 0))).isEqualTo(BusinessStatus.CLOSED);
		assertThat(store.businessStatusAt(at(TUESDAY.plusDays(1), 12, 0))).isEqualTo(BusinessStatus.OPEN);
	}

	private static Store store(LocalTime open, LocalTime close, LocalTime breakStart, LocalTime breakEnd,
			Set<DayOfWeek> closedDays) {
		return Store.builder()
				.name("가게")
				.openTime(open)
				.closeTime(close)
				.breakStart(breakStart)
				.breakEnd(breakEnd)
				.closedDays(closedDays)
				.build();
	}

	private static LocalDateTime at(LocalDate date, int hour, int minute) {
		return date.atTime(hour, minute);
	}

}
