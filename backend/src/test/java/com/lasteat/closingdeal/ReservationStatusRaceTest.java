package com.lasteat.closingdeal;

import static org.assertj.core.api.Assertions.assertThat;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.closingdeal.service.OwnerReservationService;
import com.lasteat.closingdeal.service.ReservationService;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
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

// 손님 취소와 사장님 수령 처리가 동시에 들어와도 하나만 반영되고 재고가 상태와 맞는다 (Reservation @Version).
@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, ClosingDealTestSupport.FixedClockConfig.class})
class ReservationStatusRaceTest extends ClosingDealTestSupport {

	private static final int ROUNDS = 10;

	private static final int STOCK = 20;

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private OwnerReservationService ownerReservationService;

	private Member owner;

	private Member customer;

	private Store store;

	private ClosingDeal deal;

	@BeforeEach
	void setUp() {
		cleanUp();
		owner = saveMember("owner", MemberRole.OWNER);
		customer = saveMember("customer", MemberRole.CONSUMER);
		store = saveStore(owner, "연남 온기", StoreCategory.KOREAN, LAT, LNG);
		Menu menu = saveMenu(store, "연어 덮밥", 15000);
		deal = saveDeal(menu, 9000, STOCK, NOW.minusMinutes(10), NOW.plusHours(1));
	}

	@Test
	void 취소와_수령_처리가_동시에_들어와도_하나만_반영된다() throws Exception {
		List<String> unexpected = new CopyOnWriteArrayList<>();
		for (int round = 0; round < ROUNDS; round++) {
			Long reservationId = reservationService.reserve(customer.getId(), deal.getId(), 1).id();
			AtomicInteger succeeded = new AtomicInteger();
			runTogether(
					() -> reservationService.cancel(customer.getId(), reservationId),
					() -> ownerReservationService.pickUp(store.getId(), reservationId, owner.getId()),
					succeeded, unexpected);
			assertThat(succeeded.get()).as("round %d", round).isEqualTo(1);
		}
		assertThat(unexpected).isEmpty();

		// 취소된 예약 수량만 재고로 돌아왔다
		List<Reservation> all = reservationRepository.findAll();
		long pickedUp = all.stream().filter(r -> r.getStatus() == ReservationStatus.PICKED_UP).count();
		long canceled = all.stream().filter(r -> r.getStatus() == ReservationStatus.CANCELED).count();
		assertThat(pickedUp + canceled).isEqualTo(ROUNDS);
		assertThat(closingDealRepository.findById(deal.getId()).orElseThrow().getStock())
				.isEqualTo(STOCK - pickedUp);
	}

	private static void runTogether(Callable<?> first, Callable<?> second, AtomicInteger succeeded,
			List<String> unexpected) throws InterruptedException {
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		for (Callable<?> task : List.of(first, second)) {
			pool.submit(() -> {
				start.await();
				try {
					task.call();
					succeeded.incrementAndGet();
				} catch (BusinessException expected) {
					// 늦은 쪽은 RESERVATION_NOT_CANCELABLE 또는 INVALID_RESERVATION_STATUS
				} catch (Exception e) {
					unexpected.add(e.getClass().getName() + ": " + e.getMessage());
				}
				return null;
			});
		}
		start.countDown();
		pool.shutdown();
		assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
	}

}
