package com.lasteat.global.converter;

import jakarta.persistence.Converter;
import java.time.DayOfWeek;
import java.util.Arrays;

// 요일을 API 명세의 요일 코드(MON~SUN)로 저장한다.
@Converter
public class DayOfWeekSetConverter extends CodeSetConverter<DayOfWeek> {

	private static final int CODE_LENGTH = 3;

	public DayOfWeekSetConverter() {
		super(DayOfWeekSetConverter::toCode, DayOfWeekSetConverter::fromCode);
	}

	public static String toCode(DayOfWeek day) {
		return day.name().substring(0, CODE_LENGTH);
	}

	public static DayOfWeek fromCode(String code) {
		return Arrays.stream(DayOfWeek.values())
				.filter(day -> toCode(day).equals(code))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("알 수 없는 요일 코드입니다: " + code));
	}

}
