package com.lasteat.store.entity;

import com.lasteat.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 가게 사진. sortOrder 0이 대표 사진
@Getter
@Entity
@Table(name = "store_image", indexes = {
		@Index(name = "idx_store_image_store_order", columnList = "store_id, sort_order")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreImage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false, length = 500)
	private String imageUrl;

	@Column(nullable = false)
	private int sortOrder;

	@Builder
	private StoreImage(Store store, String imageUrl, int sortOrder) {
		if (sortOrder < 0) {
			throw new IllegalArgumentException("사진 순서는 0 이상이어야 합니다.");
		}
		this.store = store;
		this.imageUrl = imageUrl;
		this.sortOrder = sortOrder;
	}

}
