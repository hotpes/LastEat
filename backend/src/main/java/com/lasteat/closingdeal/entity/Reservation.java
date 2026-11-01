package com.lasteat.closingdeal.entity;

import com.lasteat.global.entity.BaseTimeEntity;
import com.lasteat.member.entity.Member;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "closing_deal_id", nullable = false)
	private ClosingDeal closingDeal;

	@Column(nullable = false)
	private int quantity;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private ReservationStatus status;

	private LocalDateTime pickedUpAt;

	@Builder
	private Reservation(Member member, ClosingDeal closingDeal, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
		}
		this.member = member;
		this.closingDeal = closingDeal;
		this.quantity = quantity;
		this.status = ReservationStatus.RESERVED;
	}

	public void pickUp(LocalDateTime pickedUpAt) {
		validateReserved();
		this.status = ReservationStatus.PICKED_UP;
		this.pickedUpAt = pickedUpAt;
	}

	public void cancel() {
		validateReserved();
		this.status = ReservationStatus.CANCELED;
	}

	public void markNoShow() {
		validateReserved();
		this.status = ReservationStatus.NO_SHOW;
	}

	private void validateReserved() {
		if (status != ReservationStatus.RESERVED) {
			throw new IllegalStateException("예약 상태에서만 변경할 수 있습니다. 현재 상태: " + status);
		}
	}

}
