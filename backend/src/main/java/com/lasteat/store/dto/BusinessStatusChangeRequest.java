package com.lasteat.store.dto;

import com.lasteat.store.entity.BusinessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

public record BusinessStatusChangeRequest(
		@Schema(description = "OPEN·BREAK·CLOSED는 오늘 하루 이 상태로 표시 (다음 날 0시에 자동 계산으로 돌아감), "
				+ "AUTO는 영업시간 기준 자동 계산", example = "CLOSED")
		@NotNull(message = "영업 상태를 선택해 주세요.")
		Status status) {

	public enum Status {
		OPEN,
		BREAK,
		CLOSED,
		AUTO;

		// AUTO면 빈 값
		public Optional<BusinessStatus> toOverride() {
			return this == AUTO ? Optional.empty() : Optional.of(BusinessStatus.valueOf(name()));
		}
	}

}
