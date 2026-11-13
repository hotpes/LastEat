package com.lasteat.store.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StoreTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 3);

	@Test
	void 오늘_정한_영업_상태는_오늘만_적용된다() {
		Store store = Store.builder().name("가게").build();

		store.overrideBusinessStatus(BusinessStatus.CLOSED, TODAY);

		assertThat(store.businessStatusOverrideOn(TODAY)).contains(BusinessStatus.CLOSED);
		assertThat(store.businessStatusOverrideOn(TODAY.plusDays(1))).isEmpty();
	}

	@Test
	void 영업_상태_지정을_해제하면_자동_계산으로_돌아간다() {
		Store store = Store.builder().name("가게").build();
		store.overrideBusinessStatus(BusinessStatus.BREAK, TODAY);

		store.clearBusinessStatusOverride();

		assertThat(store.businessStatusOverrideOn(TODAY)).isEmpty();
		assertThat(store.getBusinessStatusOverride()).isNull();
		assertThat(store.getBusinessStatusOverrideDate()).isNull();
	}

	@Test
	void 휴무일과_태그를_넘기지_않으면_빈_목록이다() {
		Store store = Store.builder().name("가게").build();

		assertThat(store.getClosedDays()).isEmpty();
		assertThat(store.getTags()).isEmpty();
		assertThat(store.isRecommended()).isFalse();
	}

	@Test
	void 넘긴_목록을_바꿔도_가게의_값은_바뀌지_않는다() {
		List<String> tags = new ArrayList<>(List.of("포장 가능"));
		Store store = Store.builder().name("가게").tags(tags).closedDays(Set.of(DayOfWeek.MONDAY)).build();

		tags.add("주차 가능");

		assertThat(store.getTags()).containsExactly("포장 가능");
		assertThatThrownBy(() -> store.getTags().add("x")).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void 태그는_5개를_넘을_수_없다() {
		List<String> tags = List.of("a", "b", "c", "d", "e", "f");

		assertThatThrownBy(() -> Store.builder().name("가게").tags(tags).build())
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 태그에는_쉼표를_넣을_수_없다() {
		assertThatThrownBy(() -> Store.builder().name("가게").tags(List.of("포장,배달")).build())
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 태그는_10자를_넘을_수_없다() {
		assertThatThrownBy(() -> Store.builder().name("가게").tags(List.of("열한글자가넘는긴태그입니다")).build())
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 메뉴_품절을_표시하고_해제할_수_있다() {
		Menu menu = Menu.builder().name("메뉴").price(10000).build();

		menu.changeSoldOut(true);
		assertThat(menu.isSoldOut()).isTrue();

		menu.changeSoldOut(false);
		assertThat(menu.isSoldOut()).isFalse();
	}

	@Test
	void 가게_사진_순서는_음수일_수_없다() {
		assertThatThrownBy(() -> StoreImage.builder().imageUrl("https://x/1.jpg").sortOrder(-1).build())
				.isInstanceOf(IllegalArgumentException.class);
	}

}
