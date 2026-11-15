package com.lasteat.benefit.dto;

import com.lasteat.benefit.entity.Benefit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 할인 공지 게시·수정 요청. 입력 제한은 사장님 앱 화면과 같다. 앞뒤 공백은 엔티티에서 지운다.
public record BenefitRequest(
		@Schema(description = "공지 제목", example = "평일 점심 10% 할인")
		@NotBlank(message = "공지 제목을 입력해 주세요.")
		@Size(max = Benefit.MAX_TITLE_LENGTH, message = "공지 제목은 60자 이하로 입력해 주세요.")
		String title,

		@Schema(description = "할인 내용·이용 조건", example = "평일 점심 식사 메뉴 주문 시 10% 할인해 드려요. 주류 제외.")
		@NotBlank(message = "할인 내용과 이용 조건을 입력해 주세요.")
		@Size(max = Benefit.MAX_CONTENT_LENGTH, message = "할인 내용은 1000자 이하로 입력해 주세요.")
		String content,

		@Schema(description = "안내 기간", example = "11월 한 달, 평일 11~14시")
		@NotBlank(message = "안내 기간을 입력해 주세요.")
		@Size(max = Benefit.MAX_PERIOD_TEXT_LENGTH, message = "안내 기간은 100자 이하로 입력해 주세요.")
		String periodText) {
}
