package com.lasteat.benefit.entity;

import com.lasteat.global.entity.BaseTimeEntity;
import com.lasteat.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 할인 공지 (일반 할인·제휴 혜택). 사장님이 글로 쓰고, 게시하면 바로 손님에게 보인다.
// 안내 기간은 글이라서 서버가 기간 종료를 판단하지 않는다. 예약이 필요한 마감할인은 ClosingDeal.
@Getter
@Entity
@Table(name = "benefit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Benefit extends BaseTimeEntity {

	// 사장님 앱 입력 제한과 같다
	public static final int MAX_TITLE_LENGTH = 60;

	public static final int MAX_CONTENT_LENGTH = 1000;

	public static final int MAX_PERIOD_TEXT_LENGTH = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false, length = MAX_TITLE_LENGTH)
	private String title;

	// 할인 내용·이용 조건
	@Column(nullable = false, length = MAX_CONTENT_LENGTH)
	private String content;

	// 안내 기간 (예: "11월 한 달", "상시")
	@Column(nullable = false, length = MAX_PERIOD_TEXT_LENGTH)
	private String periodText;

	@Column(nullable = false)
	private boolean published;

	// 제휴 혜택 여부. 운영진이 DB에서 지정한다
	@Column(nullable = false)
	private boolean partner;

	// 처음 게시한 시각. 수정해도 바뀌지 않는다
	@Column(nullable = false)
	private LocalDateTime publishedAt;

	@Builder
	private Benefit(Store store, String title, String content, String periodText, LocalDateTime publishedAt) {
		this.store = Objects.requireNonNull(store, "가게가 필요합니다.");
		this.publishedAt = Objects.requireNonNull(publishedAt, "게시 시각이 필요합니다.");
		apply(title, content, periodText);
		this.published = true;
	}

	public void update(String title, String content, String periodText) {
		apply(title, content, periodText);
	}

	public void changePublished(boolean published) {
		this.published = published;
	}

	// 운영진용 (관리자 화면은 1차 범위 밖)
	public void changePartner(boolean partner) {
		this.partner = partner;
	}

	private void apply(String title, String content, String periodText) {
		this.title = requireText(title, MAX_TITLE_LENGTH, "공지 제목");
		this.content = requireText(content, MAX_CONTENT_LENGTH, "할인 내용");
		this.periodText = requireText(periodText, MAX_PERIOD_TEXT_LENGTH, "안내 기간");
	}

	// 앞뒤 공백을 지우고, 비었거나 너무 길면 예외
	private static String requireText(String value, int maxLength, String name) {
		String stripped = value == null ? "" : value.strip();
		if (stripped.isEmpty()) {
			throw new IllegalArgumentException(name + "이(가) 비어 있습니다.");
		}
		if (stripped.length() > maxLength) {
			throw new IllegalArgumentException(name + "은(는) " + maxLength + "자 이하여야 합니다.");
		}
		return stripped;
	}

}
