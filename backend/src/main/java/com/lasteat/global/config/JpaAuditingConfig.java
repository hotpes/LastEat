package com.lasteat.global.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// createdAt·updatedAt도 Clock 빈을 따른다 (예약 시각 = 예약의 createdAt). 슬라이스 테스트처럼 Clock 빈이 없으면 시스템 시계.
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

	@Bean
	DateTimeProvider auditingDateTimeProvider(ObjectProvider<Clock> clock) {
		return () -> Optional.of(LocalDateTime.now(clock.getIfAvailable(Clock::systemDefaultZone)));
	}

}
