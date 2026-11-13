package com.lasteat.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// 마감할인 자동 마감 같은 주기 작업. 테스트에서는 lasteat.scheduler.enabled=false로 끈다.
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "lasteat.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {

}
