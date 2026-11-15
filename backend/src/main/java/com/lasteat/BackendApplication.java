package com.lasteat;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

	// 엔티티 시각(LocalDateTime)은 모두 한국 시간 기준. 서버 OS 시간대와 상관없이 고정한다.
	public static final String TIME_ZONE = "Asia/Seoul";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE));
		SpringApplication.run(BackendApplication.class, args);
	}

}
