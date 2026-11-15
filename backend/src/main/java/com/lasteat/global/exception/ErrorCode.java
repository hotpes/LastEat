package com.lasteat.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// API 명세 부록 B와 같은 목록을 유지한다. message는 앱 화면에 그대로 보여 줘도 되는 문장이다.
@Getter
public enum ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
	INVALID_FILE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다. 다시 로그인해 주세요."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다."),
	KAKAO_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "카카오 로그인에 실패했습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "사장님 계정만 이용할 수 있습니다."),
	NOT_STORE_OWNER(HttpStatus.FORBIDDEN, "내 가게가 아닙니다."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."),
	MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
	BENEFIT_NOT_FOUND(HttpStatus.NOT_FOUND, "할인 공지를 찾을 수 없습니다."),
	CLOSING_DEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "마감할인을 찾을 수 없습니다."),
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
	OWNER_HAS_STORE(HttpStatus.CONFLICT, "연결된 가게가 있어 탈퇴할 수 없습니다."),
	CLOSING_DEAL_NOT_OPEN(HttpStatus.CONFLICT, "지금은 예약하거나 바꿀 수 없는 마감할인입니다."),
	OUT_OF_STOCK(HttpStatus.CONFLICT, "남은 수량이 부족합니다. 수량을 확인해 주세요."),
	ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약한 마감할인입니다."),
	RESERVATION_BUSY(HttpStatus.CONFLICT, "예약이 몰리고 있어요. 잠시 후 다시 시도해 주세요."),
	RESERVATION_NOT_CANCELABLE(HttpStatus.CONFLICT, "취소할 수 없는 예약입니다."),
	INVALID_RESERVATION_STATUS(HttpStatus.CONFLICT, "이미 처리된 예약입니다."),
	FILE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "이미지는 10MB 이하만 올릴 수 있습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

	private final HttpStatus status;

	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

}
