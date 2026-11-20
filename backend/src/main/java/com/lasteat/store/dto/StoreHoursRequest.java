package com.lasteat.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

// 사장님 영업시간 수정 요청. 쉬는 시간 짝·순서와 요일 코드는 서비스에서 확인한다.
public record StoreHoursRequest(
		@Schema(description = "여는 시각", example = "11:00", type = "string")
		@NotNull(message = "여는 시각을 입력해 주세요.")
		LocalTime openTime,

		@Schema(description = "닫는 시각. 자정을 넘기면 여는 시각보다 이른 시각 (예: 02:00). 같으면 24시간 영업",
				example = "21:00", type = "string")
		@NotNull(message = "닫는 시각을 입력해 주세요.")
		LocalTime closeTime,

		@Schema(description = "쉬는 시간 시작. 끝과 함께 보내거나 둘 다 생략", example = "15:00", type = "string")
		LocalTime breakStart,

		@Schema(description = "쉬는 시간 끝", example = "17:00", type = "string")
		LocalTime breakEnd,

		@Schema(description = "정기 휴무 요일 코드 (MON~SUN)", example = "[\"MON\"]")
		List<String> closedDays) {
}
