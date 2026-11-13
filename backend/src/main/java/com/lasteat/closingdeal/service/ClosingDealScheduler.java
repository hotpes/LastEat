package com.lasteat.closingdeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 1분마다 수령 마감이 지난 마감할인을 마감한다. 테스트에서는 lasteat.scheduler.enabled=false로 끈다.
@Slf4j
@Component
@ConditionalOnProperty(prefix = "lasteat.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ClosingDealScheduler {

	private final ClosingDealCloser closingDealCloser;

	@Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
	public void closeExpired() {
		try {
			closingDealCloser.closeExpired();
		} catch (RuntimeException e) {
			// 손님 예약과 충돌해도 다음 실행에서 다시 처리한다
			log.warn("마감할인 자동 마감 실패, 다음 실행에서 다시 시도합니다.", e);
		}
	}

}
