package com.lasteat.store.entity;

import com.lasteat.global.converter.DayOfWeekSetConverter;
import com.lasteat.global.converter.StringListConverter;
import com.lasteat.global.entity.BaseTimeEntity;
import com.lasteat.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

	public static final int MAX_TAG_COUNT = 5;

	public static final int MAX_TAG_LENGTH = 10;

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
	// 공간 인덱스는 Flyway 마이그레이션(V1)에서 생성한다.
	@Column(nullable = false, columnDefinition = "POINT SRID 4326")
	private Point location;

	private String phone;

	@Column(length = 300)
	private String description;

	// 자정 넘김 영업이면 closeTime < openTime
	private LocalTime openTime;

	private LocalTime closeTime;

	private LocalTime breakStart;

	private LocalTime breakEnd;

	// 정기 휴무 요일. DB에는 "MON,SAT" 형식
	@Convert(converter = DayOfWeekSetConverter.class)
	@Column(nullable = false, length = 30)
	private Set<DayOfWeek> closedDays = Set.of();

	// DB에는 "혼밥 좋아요,포장 가능" 형식
	@Convert(converter = StringListConverter.class)
	@Column(nullable = false, length = 100)
	private List<String> tags = List.of();

	// 운영진 추천 가게
	@Column(nullable = false)
	private boolean recommended;

	// 사장님이 오늘 하루 직접 정한 영업 상태. 적용 날짜가 지나면 무시하고 영업시간으로 계산한다.
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(length = 20)
	private BusinessStatus businessStatusOverride;

	private LocalDate businessStatusOverrideDate;

	@Builder
	private Store(Member owner, String name, StoreCategory category, String address, Point location,
			String phone, String description, LocalTime openTime, LocalTime closeTime,
			LocalTime breakStart, LocalTime breakEnd, Set<DayOfWeek> closedDays, List<String> tags) {
		this.owner = owner;
		this.name = name;
		this.category = category;
		this.address = address;
		this.location = location;
		this.phone = phone;
		this.description = description;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.breakStart = breakStart;
		this.breakEnd = breakEnd;
		this.closedDays = closedDays == null ? Set.of() : Set.copyOf(closedDays);
		this.tags = validateTags(tags);
	}

	public void overrideBusinessStatus(BusinessStatus status, LocalDate date) {
		this.businessStatusOverride = Objects.requireNonNull(status, "영업 상태가 필요합니다.");
		this.businessStatusOverrideDate = Objects.requireNonNull(date, "적용 날짜가 필요합니다.");
	}

	// 영업시간 기준 자동 계산으로 되돌린다 (API의 AUTO)
	public void clearBusinessStatusOverride() {
		this.businessStatusOverride = null;
		this.businessStatusOverrideDate = null;
	}

	public Optional<BusinessStatus> businessStatusOverrideOn(LocalDate date) {
		if (businessStatusOverride == null || !date.equals(businessStatusOverrideDate)) {
			return Optional.empty();
		}
		return Optional.of(businessStatusOverride);
	}

	// 지금 영업 상태. 우선순위: 사장님이 정한 오늘 상태 > 정기 휴무 > 영업시간·쉬는 시간.
	// 영업시간이 등록되지 않은 가게는 CLOSED로 본다.
	public BusinessStatus businessStatusAt(LocalDateTime now) {
		Optional<BusinessStatus> override = businessStatusOverrideOn(now.toLocalDate());
		if (override.isPresent()) {
			return override.get();
		}
		if (openTime == null || closeTime == null) {
			return BusinessStatus.CLOSED;
		}
		LocalTime time = now.toLocalTime();
		Optional<LocalDate> businessDay = businessDayOf(now.toLocalDate(), time);
		if (businessDay.isEmpty() || closedDays.contains(businessDay.get().getDayOfWeek())) {
			return BusinessStatus.CLOSED;
		}
		return isOnBreak(time) ? BusinessStatus.BREAK : BusinessStatus.OPEN;
	}

	// 지금이 어느 날의 영업에 속하는지. 자정을 넘긴 시간(예: 금 18:00~02:00의 토 01:00)은 전날 영업이다.
	// 여는 시각과 닫는 시각이 같으면 24시간 영업으로 본다.
	private Optional<LocalDate> businessDayOf(LocalDate today, LocalTime time) {
		if (openTime.equals(closeTime)) {
			return Optional.of(today);
		}
		if (openTime.isBefore(closeTime)) {
			boolean open = !time.isBefore(openTime) && time.isBefore(closeTime);
			return open ? Optional.of(today) : Optional.empty();
		}
		if (!time.isBefore(openTime)) {
			return Optional.of(today);
		}
		return time.isBefore(closeTime) ? Optional.of(today.minusDays(1)) : Optional.empty();
	}

	private boolean isOnBreak(LocalTime time) {
		return breakStart != null && breakEnd != null && !time.isBefore(breakStart) && time.isBefore(breakEnd);
	}

	private static List<String> validateTags(List<String> tags) {
		if (tags == null) {
			return List.of();
		}
		if (tags.size() > MAX_TAG_COUNT) {
			throw new IllegalArgumentException("태그는 " + MAX_TAG_COUNT + "개까지 등록할 수 있습니다.");
		}
		for (String tag : tags) {
			if (tag == null || tag.isBlank() || tag.length() > MAX_TAG_LENGTH
					|| tag.contains(StringListConverter.DELIMITER)) {
				throw new IllegalArgumentException("태그는 쉼표 없이 1~" + MAX_TAG_LENGTH + "자로 입력해야 합니다: " + tag);
			}
		}
		return List.copyOf(tags);
	}

}
