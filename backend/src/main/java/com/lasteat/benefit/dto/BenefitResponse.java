package com.lasteat.benefit.dto;

import com.lasteat.BackendApplication;
import com.lasteat.benefit.entity.Benefit;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.time.ZoneId;

// 손님용 할인 공지 (목록 항목과 상세가 같은 형식)
public record BenefitResponse(
		@Schema(example = "11") Long id,
		@Schema(example = "평일 점심 10% 할인") String title,
		@Schema(description = "할인 내용·이용 조건", example = "평일 점심 식사 메뉴 주문 시 10% 할인해 드려요.") String content,
		@Schema(description = "안내 기간", example = "11월 한 달, 평일 11~14시") String periodText,
		@Schema(description = "제휴 혜택 여부", example = "true") boolean partner,
		@Schema(example = "2026-11-01T09:30:00+09:00") OffsetDateTime publishedAt,
		StoreInfo store) {

	private static final ZoneId ZONE = ZoneId.of(BackendApplication.TIME_ZONE);

	// thumbnailUrl은 사진이 없으면 null, distance(m)는 기준 좌표가 없으면 null
	public record StoreInfo(
			@Schema(example = "3") Long id,
			@Schema(example = "연남 온기") String name,
			@Schema(example = "KOREAN") StoreCategory category,
			@Schema(example = "https://cdn.lasteat.app/stores/3/main.jpg") String thumbnailUrl,
			@Schema(example = "240") Integer distance) {
	}

	public static BenefitResponse of(Benefit benefit, String thumbnailUrl, Integer distance) {
		Store store = benefit.getStore();
		return new BenefitResponse(
				benefit.getId(),
				benefit.getTitle(),
				benefit.getContent(),
				benefit.getPeriodText(),
				benefit.isPartner(),
				benefit.getPublishedAt().atZone(ZONE).toOffsetDateTime(),
				new StoreInfo(store.getId(), store.getName(), store.getCategory(), thumbnailUrl, distance));
	}

}
