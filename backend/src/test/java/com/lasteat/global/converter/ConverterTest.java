package com.lasteat.global.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConverterTest {

	private final DayOfWeekSetConverter dayConverter = new DayOfWeekSetConverter();

	private final StringListConverter stringListConverter = new StringListConverter();

	@Test
	void 요일은_요일_순서대로_세글자_코드로_저장된다() {
		String column = dayConverter.convertToDatabaseColumn(
				Set.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY));

		assertThat(column).isEqualTo("MON,SAT,SUN");
	}

	@Test
	void 요일_코드_문자열을_읽으면_요일_집합이_된다() {
		assertThat(dayConverter.convertToEntityAttribute("MON,SAT"))
				.containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);
	}

	@Test
	void 빈_집합은_빈_문자열로_저장되고_빈_문자열은_빈_집합으로_읽힌다() {
		assertThat(dayConverter.convertToDatabaseColumn(Set.of())).isEmpty();
		assertThat(dayConverter.convertToDatabaseColumn(null)).isEmpty();
		assertThat(dayConverter.convertToEntityAttribute("")).isEmpty();
		assertThat(dayConverter.convertToEntityAttribute(null)).isEmpty();
	}

	@Test
	void 알_수_없는_요일_코드는_예외가_발생한다() {
		assertThatThrownBy(() -> dayConverter.convertToEntityAttribute("MON,XYZ"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 읽은_집합은_수정할_수_없다() {
		Set<DayOfWeek> days = dayConverter.convertToEntityAttribute("MON");

		assertThatThrownBy(() -> days.add(DayOfWeek.TUESDAY)).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void 문자열_목록은_순서를_유지해서_저장하고_다시_읽는다() {
		List<String> tags = List.of("혼밥 좋아요", "포장 가능");

		String column = stringListConverter.convertToDatabaseColumn(tags);

		assertThat(column).isEqualTo("혼밥 좋아요,포장 가능");
		assertThat(stringListConverter.convertToEntityAttribute(column)).containsExactlyElementsOf(tags);
		assertThat(stringListConverter.convertToEntityAttribute("")).isEmpty();
	}

}
