package com.lasteat.benefit.dto;

import com.lasteat.BackendApplication;
import com.lasteat.benefit.entity.Benefit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.time.ZoneId;

// 내 가게 할인 공지 목록 항목 (사장님 앱 Notice)
public record OwnerBenefitResponse(
		@Schema(example = "11") Long id,
		@Schema(example = "평일 점심 10% 할인") String title,
		@Schema(description = "할인 내용·이용 조건", example = "평일 점심 식사 메뉴 주문 시 10% 할인해 드려요.") String content,
		@Schema(description = "안내 기간", example = "11월 한 달, 평일 11~14시") String periodText,
		@Schema(description = "손님에게 공개 중인지", example = "true") boolean published,
		@Schema(description = "제휴 혜택 여부 (운영진 지정)", example = "false") boolean partner,
		@Schema(description = "처음 게시한 시각", example = "2026-11-01T09:30:00+09:00") OffsetDateTime publishedAt) {

	private static final ZoneId ZONE = ZoneId.of(BackendApplication.TIME_ZONE);

	public static OwnerBenefitResponse from(Benefit benefit) {
		return new OwnerBenefitResponse(
				benefit.getId(),
				benefit.getTitle(),
				benefit.getContent(),
				benefit.getPeriodText(),
				benefit.isPublished(),
				benefit.isPartner(),
				benefit.getPublishedAt().atZone(ZONE).toOffsetDateTime());
	}

}
