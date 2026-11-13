package com.lasteat.global.config;

import com.lasteat.auth.jwt.JwtAuthenticationFilter;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.global.security.SecurityErrorHandler;
import com.lasteat.member.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 공개 경로만 열고 나머지는 로그인 필요, /owner/**는 사장님만.
// 공개 조회 API는 PUBLIC_GET_PATHS에 추가한다. 공개 API도 토큰을 보내면 로그인 회원 기준 값(bookmarked)이 채워진다.
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] PUBLIC_PATHS = {
			"/actuator/health",
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.html"
	};

	private static final String[] PUBLIC_GET_PATHS = {
			"/api/v1/stores",
			"/api/v1/stores/**",
			"/api/v1/benefits",
			"/api/v1/benefits/**",
			"/api/v1/closing-deals",
			"/api/v1/closing-deals/*"
	};

	private static final String[] PUBLIC_POST_PATHS = {
			"/api/v1/auth/kakao",
			"/api/v1/auth/refresh",
			"/api/v1/auth/logout"
	};

	private final SecurityErrorHandler securityErrorHandler;

	private final JwtProvider jwtProvider;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(securityErrorHandler)
						.accessDeniedHandler(securityErrorHandler))
				.addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_PATHS).permitAll()
						.requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
						.requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()
						.requestMatchers("/api/v1/owner/**").hasRole(MemberRole.OWNER.name())
						.anyRequest().authenticated())
				.build();
	}

}
