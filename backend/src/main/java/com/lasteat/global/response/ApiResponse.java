package com.lasteat.global.response;

// 모든 API가 쓰는 응답 틀: { success, data, error }
public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static ApiResponse<Void> ok() {
		return new ApiResponse<>(true, null, null);
	}

	public static ApiResponse<Void> fail(ErrorResponse error) {
		return new ApiResponse<>(false, null, error);
	}

}
