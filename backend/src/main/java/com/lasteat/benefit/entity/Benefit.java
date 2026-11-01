package com.lasteat.benefit.entity;

import com.lasteat.global.entity.BaseTimeEntity;
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
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// 일반 할인 / 제휴 혜택. 노출 전용 (발급·사용 처리 없음)
@Getter
@Entity
@Table(name = "benefit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Benefit extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private BenefitType type;

	@Column(nullable = false)
	private String title;

	// 제휴 혜택은 할인 정보가 없을 수 있음
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(length = 20)
	private DiscountType discountType;

	private Integer discountValue;

	private Integer maxDiscount;

	private Integer minOrderAmount;

	private String conditionText;

	private LocalDate startDate;

	private LocalDate endDate;

	private LocalTime availableFrom;

	private LocalTime availableTo;

	@Builder
	private Benefit(Store store, BenefitType type, String title, DiscountType discountType, Integer discountValue,
			Integer maxDiscount, Integer minOrderAmount, String conditionText, LocalDate startDate, LocalDate endDate,
			LocalTime availableFrom, LocalTime availableTo) {
		this.store = store;
		this.type = type;
		this.title = title;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.maxDiscount = maxDiscount;
		this.minOrderAmount = minOrderAmount;
		this.conditionText = conditionText;
		this.startDate = startDate;
		this.endDate = endDate;
		this.availableFrom = availableFrom;
		this.availableTo = availableTo;
	}

}
