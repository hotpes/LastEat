package com.lasteat.closingdeal.service;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// 마감할인 재고를 바꾸는 작업(예약·취소·노쇼·재고 수정)을 트랜잭션 하나로 실행한다.
// 재고는 ClosingDeal의 @Version 낙관적 락으로 지킨다. 동시에 같은 마감할인을 바꿔 충돌하거나 교착 상태가 되면
// 새 트랜잭션으로 처음부터 다시 시도하고, 계속 충돌하면 RESERVATION_BUSY.
// 호출하는 쪽은 @Transactional을 붙이지 않는다 (붙이면 재시도가 같은 트랜잭션 안에서 일어나 의미가 없다).
@Slf4j
@Component
public class DealWriteRetrier {

	static final int MAX_ATTEMPTS = 5;

	private static final int MAX_BACKOFF_MILLIS = 50;

	private final TransactionTemplate transactionTemplate;

	public DealWriteRetrier(PlatformTransactionManager transactionManager) {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public <T> T run(Supplier<T> work) {
		for (int attempt = 1; ; attempt++) {
			try {
				return transactionTemplate.execute(status -> work.get());
			} catch (ConcurrencyFailureException e) {
				// 낙관적 락 충돌(OptimisticLockingFailureException)과 MySQL 교착 상태(CannotAcquireLockException) 모두
				if (attempt >= MAX_ATTEMPTS) {
					log.warn("마감할인 재고 충돌로 {}번 시도 후 실패", attempt);
					throw new BusinessException(ErrorCode.RESERVATION_BUSY);
				}
				backOff();
			}
		}
	}

	private static void backOff() {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextInt(1, MAX_BACKOFF_MILLIS));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.RESERVATION_BUSY);
		}
	}

}
