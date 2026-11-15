package com.lasteat.auth.kakao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// appId: 카카오 개발자 콘솔 > 내 애플리케이션 > 앱 ID (숫자). 비밀값은 아니지만 환경마다 다를 수 있어 설정으로 둔다.
// 받은 카카오 토큰이 우리 앱에서 발급된 것인지 확인하는 데 쓴다.
@Validated
@ConfigurationProperties("kakao")
public record KakaoProperties(
		@NotBlank String baseUrl,
		@NotNull Long appId,
		@NotNull Duration timeout) {
}
