package com.lasteat.closingdeal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ClosingDealTest {

	private final Store store = Store.builder().name("가게").build();

	private final Menu menu = Menu.builder().store(store).name("메뉴").price(10000).build();

	@Test
	void 다른_가게의_메뉴로는_생성할_수_없다() {
		Store otherStore = Store.builder().name("다른 가게").build();

		assertThatThrownBy(() -> dealWithStock(otherStore, menu, 3))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 저장된_같은_가게면_인스턴스가_달라도_생성된다() {
		Store sameStore = Store.builder().name("가게").build();
		ReflectionTestUtils.setField(store, "id", 1L);
		ReflectionTestUtils.setField(sameStore, "id", 1L);

		ClosingDeal deal = dealWithStock(sameStore, menu, 3);

		assertThat(deal.getStatus()).isEqualTo(ClosingDealStatus.SCHEDULED);
	}

	@Test
	void 재고를_모두_차감하면_SOLD_OUT이_된다() {
		ClosingDeal deal = dealWithStock(store, menu, 3);

		deal.decreaseStock(3);

		assertThat(deal.getStock()).isZero();
		assertThat(deal.getStatus()).isEqualTo(ClosingDealStatus.SOLD_OUT);
	}

	@Test
	void 재고보다_많이_차감하면_예외가_발생하고_재고는_그대로다() {
		ClosingDeal deal = dealWithStock(store, menu, 2);

		assertThatThrownBy(() -> deal.decreaseStock(3)).isInstanceOf(IllegalStateException.class);
		assertThat(deal.getStock()).isEqualTo(2);
	}

	@Test
	void 수량이_0_이하면_예외가_발생한다() {
		ClosingDeal deal = dealWithStock(store, menu, 2);

		assertThatThrownBy(() -> deal.decreaseStock(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> deal.increaseStock(-1)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void SOLD_OUT에서_재고를_복구하면_OPEN이_된다() {
		ClosingDeal deal = dealWithStock(store, menu, 1);
		deal.decreaseStock(1);

		deal.increaseStock(1);

		assertThat(deal.getStock()).isEqualTo(1);
		assertThat(deal.getStatus()).isEqualTo(ClosingDealStatus.OPEN);
	}

	@Test
	void CLOSED에서_재고를_복구해도_상태는_CLOSED다() {
		ClosingDeal deal = dealWithStock(store, menu, 0);
		ReflectionTestUtils.setField(deal, "status", ClosingDealStatus.CLOSED);

		deal.increaseStock(1);

		assertThat(deal.getStatus()).isEqualTo(ClosingDealStatus.CLOSED);
	}

	@Test
	void 상태는_시각과_재고로_계산한다() {
		ClosingDeal deal = dealWithStock(store, menu, 2);

		assertThat(deal.statusAt(OPEN_AT.minusMinutes(1))).isEqualTo(ClosingDealStatus.SCHEDULED);
		assertThat(deal.statusAt(OPEN_AT)).isEqualTo(ClosingDealStatus.OPEN);
		assertThat(deal.statusAt(OPEN_AT.plusMinutes(59))).isEqualTo(ClosingDealStatus.OPEN);
		assertThat(deal.statusAt(OPEN_AT.plusHours(1))).isEqualTo(ClosingDealStatus.CLOSED);

		deal.decreaseStock(2);
		assertThat(deal.statusAt(OPEN_AT)).isEqualTo(ClosingDealStatus.SOLD_OUT);
		assertThat(deal.statusAt(OPEN_AT.minusMinutes(1))).isEqualTo(ClosingDealStatus.SCHEDULED);
	}

	@Test
	void 손님에게는_예약_가능이나_품절일_때만_보인다() {
		ClosingDeal deal = dealWithStock(store, menu, 0);

		assertThat(deal.isVisibleAt(OPEN_AT)).isTrue();
		assertThat(deal.isVisibleAt(OPEN_AT.minusMinutes(1))).isFalse();
		assertThat(deal.isVisibleAt(OPEN_AT.plusHours(1))).isFalse();
	}

	@Test
	void 조기_마감하면_수령_마감_전이어도_CLOSED다() {
		ClosingDeal deal = dealWithStock(store, menu, 3);

		deal.close();

		assertThat(deal.getStatus()).isEqualTo(ClosingDealStatus.CLOSED);
		assertThat(deal.statusAt(OPEN_AT)).isEqualTo(ClosingDealStatus.CLOSED);
		assertThat(deal.isVisibleAt(OPEN_AT)).isFalse();
	}

	@Test
	void 재고를_직접_바꾸면_0은_품절이고_품절에서_늘리면_다시_예약_가능이다() {
		ClosingDeal deal = dealWithStock(store, menu, 3);

		deal.changeStock(0);
		assertThat(deal.getStatus()).isEqualTo(ClosingDealStatus.SOLD_OUT);

		deal.changeStock(5);
		assertThat(deal.getStock()).isEqualTo(5);
		assertThat(deal.getStatus()).isEqualTo(ClosingDealStatus.OPEN);
	}

	@Test
	void 마감된_재고는_바꿀_수_없고_음수_재고도_안_된다() {
		ClosingDeal deal = dealWithStock(store, menu, 3);

		assertThatThrownBy(() -> deal.changeStock(-1)).isInstanceOf(IllegalArgumentException.class);
		deal.close();
		assertThatThrownBy(() -> deal.changeStock(1)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void 수령_마감은_노출_시작보다_늦어야_한다() {
		assertThatThrownBy(() -> ClosingDeal.builder().store(store).menu(menu).originalPrice(10000).salePrice(5000)
				.stock(1).openAt(OPEN_AT).pickupEndAt(OPEN_AT).build())
				.isInstanceOf(IllegalArgumentException.class);
	}

	private static final LocalDateTime OPEN_AT = LocalDateTime.of(2026, 11, 2, 20, 0);

	private static ClosingDeal dealWithStock(Store store, Menu menu, int stock) {
		LocalDateTime now = OPEN_AT;
		return ClosingDeal.builder()
				.store(store)
				.menu(menu)
				.originalPrice(10000)
				.salePrice(5000)
				.stock(stock)
				.openAt(now)
				.pickupEndAt(now.plusHours(1))
				.build();
	}

}
