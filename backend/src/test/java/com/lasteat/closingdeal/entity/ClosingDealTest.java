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

	private static ClosingDeal dealWithStock(Store store, Menu menu, int stock) {
		LocalDateTime now = LocalDateTime.of(2026, 11, 2, 20, 0);
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
