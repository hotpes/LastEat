package com.lasteat.global.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;

// 문자열 목록을 쉼표 구분 문자열 한 컬럼에 저장한다. 값에 쉼표가 들어가면 안 된다 (엔티티에서 검증).
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

	public static final String DELIMITER = ",";

	@Override
	public String convertToDatabaseColumn(List<String> values) {
		if (values == null || values.isEmpty()) {
			return "";
		}
		return String.join(DELIMITER, values);
	}

	@Override
	public List<String> convertToEntityAttribute(String column) {
		if (column == null || column.isBlank()) {
			return List.of();
		}
		return Arrays.stream(column.split(DELIMITER)).map(String::trim).toList();
	}

}
