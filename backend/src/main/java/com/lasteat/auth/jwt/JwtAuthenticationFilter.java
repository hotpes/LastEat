package com.lasteat.auth.jwt;

import com.lasteat.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Authorization: Bearer {accessToken}이 있으면 로그인 상태로 만든다.
// 토큰이 잘못됐거나 만료되면 로그인 없이 계속 진행하고, 로그인이 필요한 주소면 SecurityErrorHandler가
// 여기서 남긴 에러 코드(INVALID_TOKEN / EXPIRED_TOKEN)로 401을 응답한다.
// 서블릿 필터로 따로 등록되지 않도록 @Component로 만들지 않고 SecurityConfig에서 생성한다.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String AUTH_ERROR_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".error";

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtProvider jwtProvider;

	public JwtAuthenticationFilter(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			authenticate(request, header.substring(BEARER_PREFIX.length()).trim());
		}
		chain.doFilter(request, response);
	}

	private void authenticate(HttpServletRequest request, String token) {
		try {
			AuthMember member = jwtProvider.parseAccessToken(token);
			var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.role().name()));
			var authentication = UsernamePasswordAuthenticationToken.authenticated(member, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (BusinessException e) {
			request.setAttribute(AUTH_ERROR_ATTRIBUTE, e.getErrorCode());
		}
	}

}
