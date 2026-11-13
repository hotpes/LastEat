package com.lasteat.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger UI: /swagger-ui/index.html
// 로그인이 필요한 API는 컨트롤러나 메서드에 @SecurityRequirement(name = OpenApiConfig.BEARER)를 붙인다.
@Configuration
public class OpenApiConfig {

	public static final String BEARER = "bearerAuth";

	@Bean
	OpenAPI openApi() {
		return new OpenAPI()
				.info(new Info()
						.title("LastEat API")
						.version("v0.2")
						.description("응답은 모두 { success, data, error } 형식이다. 에러 코드는 docs/api-spec.md 부록 B."))
				.components(new Components()
						.addSecuritySchemes(BEARER, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}

}
