package com.lasteat.global.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

	@Test
	void 페이지_정보를_명세_형식으로_옮긴다() {
		PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 5);

		PageResponse<String> response = PageResponse.from(page);

		assertThat(response.content()).containsExactly("a", "b");
		assertThat(response.page()).isZero();
		assertThat(response.size()).isEqualTo(2);
		assertThat(response.totalElements()).isEqualTo(5);
		assertThat(response.hasNext()).isTrue();
	}

	@Test
	void 마지막_페이지면_다음이_없다() {
		PageImpl<String> page = new PageImpl<>(List.of("e"), PageRequest.of(2, 2), 5);

		assertThat(PageResponse.from(page).hasNext()).isFalse();
	}

}
