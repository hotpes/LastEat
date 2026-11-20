package com.lasteat.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 업로드한 이미지를 /uploads/**로 내준다 (공개, SecurityConfig). 파일 이름이 UUID라 한 번 올린 파일은 바뀌지 않는다.
// @WebMvcTest 슬라이스는 WebMvcConfigurer만 올리고 @ConfigurationPropertiesScan은 하지 않아서 여기서 직접 등록한다
@Configuration
@EnableConfigurationProperties(UploadProperties.class)
@RequiredArgsConstructor
public class UploadResourceConfig implements WebMvcConfigurer {

	private static final int CACHE_SECONDS = 60 * 60 * 24 * 30;

	private final UploadProperties properties;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 폴더가 아직 없으면 toUri()가 '/'로 끝나지 않아 하위 파일을 못 찾으므로 직접 붙인다
		String location = properties.root().toUri().toString();
		registry.addResourceHandler(ImageStorage.URL_PREFIX + "**")
				.addResourceLocations(location.endsWith("/") ? location : location + "/")
				.setCachePeriod(CACHE_SECONDS);
	}

}
