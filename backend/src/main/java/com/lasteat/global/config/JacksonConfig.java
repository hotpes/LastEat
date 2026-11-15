package com.lasteat.global.config;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

// API 명세 공통 규칙: 시각은 "14:00" (초 없음). 요청의 "14:00"은 기본 설정으로 읽힌다.
@Configuration
public class JacksonConfig {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	@Bean
	JacksonModule timeFormatModule() {
		return new SimpleModule("lasteat-time")
				.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMAT));
	}

}
