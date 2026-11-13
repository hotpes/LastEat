package com.lasteat.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lasteat.global.exception.ErrorCode;
import java.util.List;

// fields는 입력값 오류(INVALID_INPUT)일 때만 응답에 나간다.
public record ErrorResponse(
		String code,
		String message,
		@JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldErrorDetail> fields) {

	public ErrorResponse {
		fields = fields == null ? List.of() : List.copyOf(fields);
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return of(errorCode, List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> fields) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fields);
	}

	public record FieldErrorDetail(String field, String reason) {
	}

}
