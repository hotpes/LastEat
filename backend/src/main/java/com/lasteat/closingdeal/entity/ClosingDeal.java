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
