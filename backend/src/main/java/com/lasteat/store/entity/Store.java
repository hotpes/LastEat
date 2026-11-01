package com.lasteat.store.entity;

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
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Getter
@Entity
@Table(name = "store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 운영자가 직접 등록한 가게는 사장님 계정이 없을 수 있음
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private Member owner;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private StoreCategory category;

	@Column(nullable = false)
	private String address;

	// MySQL SRID 4326은 (위도, 경도) 축 순서. JTS Point는 x=경도, y=위도로 만든다.
	// 공간 인덱스는 @Table(indexes)로 생성되지 않으므로 수동 DDL로 건다: CREATE SPATIAL INDEX idx_store_location ON store (location);
	@Column(nullable = false, columnDefinition = "POINT SRID 4326")
	private Point location;

	private String phone;

	private String description;

	private String thumbnailUrl;

	// 자정 넘김 영업이면 closeTime < openTime
	private LocalTime openTime;

	private LocalTime closeTime;

	private LocalTime breakStart;

	private LocalTime breakEnd;

	// 예: "MON", "SAT,SUN"
	private String closedDays;

	@Column(nullable = false, precision = 2, scale = 1)
	private BigDecimal ratingAvg = new BigDecimal("0.0");

	@Column(nullable = false)
	private int reviewCount = 0;

	@Builder
	private Store(Member owner, String name, StoreCategory category, String address, Point location,
			String phone, String description, String thumbnailUrl, LocalTime openTime, LocalTime closeTime,
			LocalTime breakStart, LocalTime breakEnd, String closedDays) {
		this.owner = owner;
		this.name = name;
		this.category = category;
		this.address = address;
		this.location = location;
		this.phone = phone;
		this.description = description;
		this.thumbnailUrl = thumbnailUrl;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.breakStart = breakStart;
		this.breakEnd = breakEnd;
		this.closedDays = closedDays;
	}

}
