package com.lasteat.closingdeal;

import static org.assertj.core.api.Assertions.assertThat;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.service.ReservationService;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

// 여러 손님이 동시에 예약해도 재고보다 많이 팔리지 않는다 (ClosingDeal @Version + DealWriteRetrier).
@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, ClosingDealTestSupport.FixedClockConfig.class})
class ReservationConcurrencyTest extends ClosingDealTestSupport {

	private static final int STOCK = 3;

	private static final int CUSTOMERS = 10;

	@Autowired
	private ReservationService reservationService;

	private ClosingDeal deal;

	private final List<Member> customers = new ArrayList<>();

	@BeforeEach
	void setUp() {
		cleanUp();
		customers.clear();
		Store store = saveStore(null, "연남 온기", StoreCategory.KOREAN, LAT, LNG);
		deal = saveDeal(saveMenu(store, "연어 덮밥", 15000), 9000, STOCK, NOW.minusMinutes(10), NOW.plusHours(1));
		for (int i = 0; i < CUSTOMERS; i++) {
			customers.add(saveMember("customer-" + i, MemberRole.CONSUMER));
		}
	}

	@Test
	void 동시에_예약해도_재고보다_많이_팔리지_않는다() throws Exception {
		AtomicInteger succeeded = new AtomicInteger();
		Map<ErrorCode, AtomicInteger> failures = new ConcurrentHashMap<>();
		List<String> unexpected = new java.util.concurrent.CopyOnWriteArrayList<>();
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(CUSTOMERS);
		for (Member customer : customers) {
			pool.submit(() -> {
				start.await();
				try {
					reservationService.reserve(customer.getId(), deal.getId(), 1);
					succeeded.incrementAndGet();
				} catch (BusinessException e) {
					failures.computeIfAbsent(e.getErrorCode(), code -> new AtomicInteger()).incrementAndGet();
				} catch (RuntimeException e) {
					unexpected.add(e.getClass().getName() + ": " + e.getMessage());
				}
				return null;
			});
		}
		start.countDown();
		pool.shutdown();
		assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

		assertThat(unexpected).isEmpty();
		ClosingDeal reloaded = closingDealRepository.findById(deal.getId()).orElseThrow();
		long reserved = reservationRepository.count();
		assertThat(succeeded.get()).isBetween(1, STOCK);
		assertThat(reserved).isEqualTo(succeeded.get());
		assertThat(reloaded.getStock()).isEqualTo(STOCK - succeeded.get());
		assertThat(failures.keySet()).isSubsetOf(ErrorCode.OUT_OF_STOCK, ErrorCode.RESERVATION_BUSY);
		assertThat(succeeded.get() + failures.values().stream().mapToInt(AtomicInteger::get).sum())
				.isEqualTo(CUSTOMERS);
	}

	@Test
	void 같은_손님이_동시에_두_번_눌러도_예약은_하나다() throws Exception {
		Member customer = customers.getFirst();
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		for (int i = 0; i < 2; i++) {
			pool.submit(() -> {
				start.await();
				try {
					reservationService.reserve(customer.getId(), deal.getId(), 1);
				} catch (BusinessException ignored) {
					// ALREADY_RESERVED 또는 RESERVATION_BUSY
				}
				return null;
			});
		}
		start.countDown();
		pool.shutdown();
		assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

		assertThat(reservationRepository.count()).isEqualTo(1);
		assertThat(closingDealRepository.findById(deal.getId()).orElseThrow().getStock()).isEqualTo(STOCK - 1);
	}

}
