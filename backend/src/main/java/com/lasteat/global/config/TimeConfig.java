package com.lasteat.global.config;

import com.lasteat.BackendApplication;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 현재 시각은 Clock으로 받는다. 테스트에서 고정 시각을 넣어 만료 같은 시간 규칙을 확인할 수 있다.
@Configuration
public class TimeConfig {

	@Bean
	Clock clock() {
		return Clock.system(ZoneId.of(BackendApplication.TIME_ZONE));
	}

}
