package com.lasteat.global.exception;

import com.lasteat.global.response.ErrorResponse.FieldErrorDetail;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

// 서비스에서 예상한 실패를 알릴 때 던진다. GlobalExceptionHandler가 ErrorCode대로 응답한다.
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	// INVALID_INPUT일 때 어떤 칸이 왜 틀렸는지. 없으면 빈 목록
	private final List<FieldErrorDetail> fields;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, List.of());
	}

	private BusinessException(ErrorCode errorCode, List<FieldErrorDetail> fields) {
		super(Objects.requireNonNull(errorCode, "에러 코드가 필요합니다.").name());
		this.errorCode = errorCode;
		this.fields = List.copyOf(fields);
	}

	// 요청 형식은 맞지만 다른 값과 비교해야 알 수 있는 오류 (예: 할인가가 정가보다 비쌈)
	public static BusinessException invalidInput(String field, String reason) {
		return new BusinessException(ErrorCode.INVALID_INPUT, List.of(new FieldErrorDetail(field, reason)));
	}

}
