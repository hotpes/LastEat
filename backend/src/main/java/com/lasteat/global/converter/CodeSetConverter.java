package com.lasteat.global.converter;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// 코드 값 집합을 "MON,TUE" 같은 쉼표 구분 문자열 한 컬럼에 저장한다. 빈 집합은 빈 문자열.
public abstract class CodeSetConverter<E extends Enum<E>> implements AttributeConverter<Set<E>, String> {

	private static final String DELIMITER = ",";

	private final Function<E, String> toCode;

	private final Function<String, E> fromCode;

	protected CodeSetConverter(Function<E, String> toCode, Function<String, E> fromCode) {
		this.toCode = toCode;
		this.fromCode = fromCode;
	}

	@Override
	public String convertToDatabaseColumn(Set<E> values) {
		if (values == null || values.isEmpty()) {
			return "";
		}
		return values.stream()
				.sorted(Comparator.comparingInt(Enum::ordinal))
				.map(toCode)
				.collect(Collectors.joining(DELIMITER));
	}

	@Override
	public Set<E> convertToEntityAttribute(String column) {
		if (column == null || column.isBlank()) {
			return Set.of();
		}
		return Arrays.stream(column.split(DELIMITER))
				.map(String::trim)
				.map(fromCode)
				.collect(Collectors.toUnmodifiableSet());
	}

}
