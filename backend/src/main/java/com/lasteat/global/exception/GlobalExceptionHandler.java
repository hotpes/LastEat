package com.lasteat.global.exception;

import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.ErrorResponse;
import com.lasteat.global.response.ErrorResponse.FieldErrorDetail;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// 컨트롤러에서 나온 예외를 공통 응답 틀로 바꾼다. 내부 메시지·스택은 응답에 넣지 않고 로그에만 남긴다.
// 도메인 엔티티가 던지는 IllegalArgumentException은 여기서 잡지 않는다. 서비스가 BusinessException으로 바꿔 던진다.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String REQUIRED = "필수 값입니다.";

	private static final String WRONG_FORMAT = "형식이 올바르지 않습니다.";

	@ExceptionHandler(BusinessException.class)
	ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
		return respond(e.getErrorCode(), e.getFields());
	}

	// @RequestBody, @ModelAttribute(쿼리 파라미터 묶음) 검증 실패. MethodArgumentNotValidException도 BindException이다.
	// 형 변환 실패(예: 없는 카테고리 코드)는 내부 메시지 대신 "형식이 올바르지 않습니다."
	@ExceptionHandler(BindException.class)
	ResponseEntity<ApiResponse<Void>> handleBindValidation(BindException e) {
		List<FieldErrorDetail> fields = e.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorDetail(error.getField(),
						error.isBindingFailure() ? WRONG_FORMAT : reasonOf(error)))
				.toList();
		return respond(ErrorCode.INVALID_INPUT, fields);
	}

	// @RequestParam, @PathVariable 검증 실패
	@ExceptionHandler(HandlerMethodValidationException.class)
	ResponseEntity<ApiResponse<Void>> handleParameterValidation(HandlerMethodValidationException e) {
		List<FieldErrorDetail> fields = e.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream()
						.map(error -> new FieldErrorDetail(result.getMethodParameter().getParameterName(),
								reasonOf(error))))
				.toList();
		return respond(ErrorCode.INVALID_INPUT, fields);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
		return respond(ErrorCode.INVALID_INPUT, List.of(new FieldErrorDetail(e.getParameterName(), REQUIRED)));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return respond(ErrorCode.INVALID_INPUT, List.of(new FieldErrorDetail(e.getName(), WRONG_FORMAT)));
	}

	// JSON 문법 오류, enum에 없는 값 등
	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
		log.debug("요청 본문을 읽을 수 없음", e);
		return respond(ErrorCode.INVALID_INPUT);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ResponseEntity<ApiResponse<Void>> handleTooLarge(MaxUploadSizeExceededException e) {
		return respond(ErrorCode.FILE_TOO_LARGE);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
		return respond(ErrorCode.RESOURCE_NOT_FOUND);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return respond(ErrorCode.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
		log.error("처리하지 못한 예외", e);
		return respond(ErrorCode.INTERNAL_ERROR);
	}

	private static String reasonOf(MessageSourceResolvable error) {
		return error.getDefaultMessage() == null ? WRONG_FORMAT : error.getDefaultMessage();
	}

	private static ResponseEntity<ApiResponse<Void>> respond(ErrorCode errorCode) {
		return respond(errorCode, List.of());
	}

	private static ResponseEntity<ApiResponse<Void>> respond(ErrorCode errorCode, List<FieldErrorDetail> fields) {
		return ResponseEntity.status(errorCode.getStatus())
				.body(ApiResponse.fail(ErrorResponse.of(errorCode, fields)));
	}

}
