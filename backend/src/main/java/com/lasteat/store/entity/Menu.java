package com.lasteat.store.entity;

import com.lasteat.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseTimeEntity {

	public static final int MAX_NAME_LENGTH = 40;

	public static final int MAX_DESCRIPTION_LENGTH = 150;

	public static final int MAX_IMAGE_URL_LENGTH = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false)
	private String name;

	private String description;

	@Column(nullable = false)
	private int price;

	@Column(length = 500)
	private String imageUrl;

	// 대표 메뉴 여부
	@Column(nullable = false)
	private boolean signature;

	@Column(nullable = false)
	private boolean soldOut;

	// 지난 마감할인·예약이 참조하므로 행을 지우지 않고 표시만 한다 (V4)
	@Column(nullable = false)
	private boolean deleted;

	@Builder
	private Menu(Store store, String name, String description, int price, String imageUrl, boolean signature) {
		this.store = store;
		apply(name, description, price, imageUrl, signature);
	}

	// 앞뒤 공백은 지우고, 빈 설명·사진 주소는 null로 저장한다
	public void update(String name, String description, int price, String imageUrl, boolean signature) {
		apply(name, description, price, imageUrl, signature);
	}

	public void changeSoldOut(boolean soldOut) {
		this.soldOut = soldOut;
	}

	public void delete() {
		this.deleted = true;
	}

	private void apply(String name, String description, int price, String imageUrl, boolean signature) {
		String strippedName = name == null ? "" : name.strip();
		if (strippedName.isEmpty() || strippedName.length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException("메뉴 이름은 1~" + MAX_NAME_LENGTH + "자여야 합니다.");
		}
		if (price < 0) {
			throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
		}
		String strippedDescription = blankToNull(description);
		if (strippedDescription != null && strippedDescription.length() > MAX_DESCRIPTION_LENGTH) {
			throw new IllegalArgumentException("메뉴 설명은 " + MAX_DESCRIPTION_LENGTH + "자 이하여야 합니다.");
		}
		String strippedImageUrl = blankToNull(imageUrl);
		if (strippedImageUrl != null && strippedImageUrl.length() > MAX_IMAGE_URL_LENGTH) {
			throw new IllegalArgumentException("사진 주소는 " + MAX_IMAGE_URL_LENGTH + "자 이하여야 합니다.");
		}
		this.name = strippedName;
		this.description = strippedDescription;
		this.price = price;
		this.imageUrl = strippedImageUrl;
		this.signature = signature;
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.strip();
	}

}
