package com.lasteat.closingdeal.entity;

import com.lasteat.global.entity.BaseTimeEntity;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "closing_deal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClosingDeal extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_id", nullable = false)
	private Menu menu;

	@Column(nullable = false)
	private int originalPrice;

	@Column(nullable = false)
	private int salePrice;

	@Column(nullable = false)
	private int stock;

	// 노출 시작
	@Column(nullable = false)
	private LocalDateTime openAt;

	// 수령 마감
	@Column(nullable = false)
	private LocalDateTime pickupEndAt;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private ClosingDealStatus status;

	// 재고 동시성 제어용 낙관적 락
	@Version
	private Long version;

	@Builder
	private ClosingDeal(Store store, Menu menu, int originalPrice, int salePrice, int stock,
			LocalDateTime openAt, LocalDateTime pickupEndAt) {
		validateMenuOfStore(store, menu);
		if (stock < 0) {
			throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
		}
		if (openAt == null || pickupEndAt == null || !pickupEndAt.isAfter(openAt)) {
			throw new IllegalArgumentException("수령 마감은 노출 시작보다 늦어야 합니다.");
		}
		this.store = store;
		this.menu = menu;
		this.originalPrice = originalPrice;
		this.salePrice = salePrice;
		this.stock = stock;
		this.openAt = openAt;
		this.pickupEndAt = pickupEndAt;
		this.status = ClosingDealStatus.SCHEDULED;
	}

	public void decreaseStock(int quantity) {
		validatePositive(quantity);
		if (stock < quantity) {
			throw new IllegalStateException("재고가 부족합니다.");
		}
		stock -= quantity;
		if (stock == 0) {
			status = ClosingDealStatus.SOLD_OUT;
		}
	}

	// 예약 취소·노쇼 시 재고 복구. CLOSED는 그대로 둔다.
	public void increaseStock(int quantity) {
		validatePositive(quantity);
		stock += quantity;
		if (status == ClosingDealStatus.SOLD_OUT) {
			status = ClosingDealStatus.OPEN;
		}
	}

	// 화면에 보여 줄 상태. 저장된 status는 조기 마감(CLOSED) 여부만 믿고, 나머지는 시각과 재고로 계산한다.
	// 그래서 상태 전환 스케줄러가 늦어도 조회·예약 결과는 같다.
	public ClosingDealStatus statusAt(LocalDateTime now) {
		if (status == ClosingDealStatus.CLOSED || !now.isBefore(pickupEndAt)) {
			return ClosingDealStatus.CLOSED;
		}
		if (now.isBefore(openAt)) {
			return ClosingDealStatus.SCHEDULED;
		}
		return stock == 0 ? ClosingDealStatus.SOLD_OUT : ClosingDealStatus.OPEN;
	}

	// 손님에게 보이는지: 예약 가능 또는 품절
	public boolean isVisibleAt(LocalDateTime now) {
		ClosingDealStatus current = statusAt(now);
		return current == ClosingDealStatus.OPEN || current == ClosingDealStatus.SOLD_OUT;
	}

	// 사장님 조기 마감 또는 수령 마감 지남. 이미 받은 예약은 그대로 둔다
	public void close() {
		this.status = ClosingDealStatus.CLOSED;
	}

	// 사장님이 남은 재고를 직접 고친다. 0이면 품절, 품절에서 늘리면 다시 예약 가능
	public void changeStock(int stock) {
		if (stock < 0) {
			throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
		}
		if (status == ClosingDealStatus.CLOSED) {
			throw new IllegalStateException("마감된 마감할인의 재고는 바꿀 수 없습니다.");
		}
		this.stock = stock;
		if (stock == 0) {
			status = ClosingDealStatus.SOLD_OUT;
		} else if (status == ClosingDealStatus.SOLD_OUT) {
			status = ClosingDealStatus.OPEN;
		}
	}

	// 할인율(%) 내림. 정가가 0이면 0
	public int discountRate() {
		return originalPrice == 0 ? 0 : (originalPrice - salePrice) * 100 / originalPrice;
	}

	private static void validateMenuOfStore(Store store, Menu menu) {
		if (store == null || menu == null || !isSameStore(store, menu.getStore())) {
			throw new IllegalArgumentException("해당 가게의 메뉴가 아닙니다.");
		}
	}

	// 저장 전이면 인스턴스로, 영속 상태면 id로 비교 (LAZY 프록시는 getId만으로 초기화되지 않음)
	private static boolean isSameStore(Store store, Store menuStore) {
		if (store == menuStore) {
			return true;
		}
		return menuStore != null && store.getId() != null && store.getId().equals(menuStore.getId());
	}

	private static void validatePositive(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
		}
	}

}
