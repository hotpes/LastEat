package com.lasteat.benefit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lasteat.store.entity.Store;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BenefitTest {

	private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 11, 1, 9, 30);

	private final Store store = Store.builder().name("가게").build();

	@Test
	void 게시하면_바로_공개되고_제휴는_아니다() {
		Benefit benefit = newBenefit("평일 점심 10% 할인", "식사 메뉴 10% 할인", "11월 한 달");

		assertThat(benefit.isPublished()).isTrue();
		assertThat(benefit.isPartner()).isFalse();
		assertThat(benefit.getPublishedAt()).isEqualTo(PUBLISHED_AT);
	}

	@Test
	void 앞뒤_공백은_지우고_저장한다() {
		Benefit benefit = newBenefit("  평일 할인 ", "\n내용\n", " 상시 ");

		assertThat(benefit.getTitle()).isEqualTo("평일 할인");
		assertThat(benefit.getContent()).isEqualTo("내용");
		assertThat(benefit.getPeriodText()).isEqualTo("상시");
	}

	@Test
	void 비어_있거나_공백만_있으면_예외가_발생한다() {
		assertThatThrownBy(() -> newBenefit(" ", "내용", "상시")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> newBenefit("제목", null, "상시")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> newBenefit("제목", "내용", "\t")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 화면_입력_제한보다_길면_예외가_발생한다() {
		assertThat(newBenefit("가".repeat(60), "나".repeat(1000), "다".repeat(100))).isNotNull();

		assertThatThrownBy(() -> newBenefit("가".repeat(61), "내용", "상시"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> newBenefit("제목", "나".repeat(1001), "상시"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> newBenefit("제목", "내용", "다".repeat(101)))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 수정해도_공개_여부와_게시_시각은_그대로다() {
		Benefit benefit = newBenefit("제목", "내용", "상시");
		benefit.changePublished(false);

		benefit.update("새 제목", "새 내용", "12월");

		assertThat(benefit.getTitle()).isEqualTo("새 제목");
		assertThat(benefit.getContent()).isEqualTo("새 내용");
		assertThat(benefit.getPeriodText()).isEqualTo("12월");
		assertThat(benefit.isPublished()).isFalse();
		assertThat(benefit.getPublishedAt()).isEqualTo(PUBLISHED_AT);
	}

	@Test
	void 공개_여부와_제휴_여부를_바꾼다() {
		Benefit benefit = newBenefit("제목", "내용", "상시");

		benefit.changePublished(false);
		benefit.changePartner(true);

		assertThat(benefit.isPublished()).isFalse();
		assertThat(benefit.isPartner()).isTrue();
	}

	@Test
	void 가게와_게시_시각이_필요하다() {
		assertThatThrownBy(() -> Benefit.builder().title("제목").content("내용").periodText("상시")
				.publishedAt(PUBLISHED_AT).build()).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> Benefit.builder().store(store).title("제목").content("내용").periodText("상시")
				.build()).isInstanceOf(NullPointerException.class);
	}

	private Benefit newBenefit(String title, String content, String periodText) {
		return Benefit.builder()
				.store(store)
				.title(title)
				.content(content)
				.periodText(periodText)
				.publishedAt(PUBLISHED_AT)
				.build();
	}

}
