package com.lasteat.auth.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// secret은 환경 변수 JWT_SECRET(운영) 또는 application-local.yaml(로컬)로 넣는다. 코드에 두지 않는다.
@Validated
@ConfigurationProperties("jwt")
public record JwtProperties(
		@NotBlank String secret,
		@NotNull Duration accessTokenTtl,
		@NotNull Duration refreshTokenTtl) {
}
