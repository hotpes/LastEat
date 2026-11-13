package com.lasteat.benefit.entity;

import com.lasteat.store.entity.Store;
import java.time.LocalDateTime;

public final class BenefitFixture {

	public static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 11, 1, 9, 30);

	private BenefitFixture() {
	}

	// 공개 중인 할인 공지
	public static Benefit notice(Store store, String title) {
		return notice(store, title, PUBLISHED_AT);
	}

	public static Benefit notice(Store store, String title, LocalDateTime publishedAt) {
		return Benefit.builder()
				.store(store)
				.title(title)
				.content("평일 점심 식사 메뉴 주문 시 10% 할인해 드려요.")
				.periodText("11월 한 달")
				.publishedAt(publishedAt)
				.build();
	}

}
