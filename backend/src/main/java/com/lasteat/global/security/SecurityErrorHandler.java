package com.lasteat.global.security;

import com.lasteat.auth.jwt.JwtAuthenticationFilter;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

// 컨트롤러에 닿기 전 보안 필터에서 막힌 요청도 공통 응답 틀로 내려 준다.
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final JsonMapper jsonMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		// 토큰을 보냈는데 잘못됐거나 만료됐으면 그 이유로, 토큰이 없으면 UNAUTHORIZED로 응답한다
		Object tokenError = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
		write(response, tokenError instanceof ErrorCode errorCode ? errorCode : ErrorCode.UNAUTHORIZED);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		write(response, ErrorCode.FORBIDDEN);
	}

	private void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		jsonMapper.writeValue(response.getOutputStream(), ApiResponse.fail(ErrorResponse.of(errorCode)));
	}

}
