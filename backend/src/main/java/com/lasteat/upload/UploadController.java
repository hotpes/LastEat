package com.lasteat.upload;

import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// 사장님 계정만 (SecurityConfig)
@Tag(name = "업로드")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

	private final ImageStorage imageStorage;

	private final UploadProperties properties;

	@Operation(summary = "이미지 업로드", description = "jpg·png·webp, 10MB 이하. 받은 url을 가게 정보 수정이나 메뉴 추가·수정에 담아 보낸다. "
			+ "에러: INVALID_FILE, FILE_TOO_LARGE, INVALID_INPUT")
	@PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UploadResponse> uploadImage(@RequestPart("file") MultipartFile file,
			@RequestParam ImagePurpose purpose) {
		String path = imageStorage.store(file, purpose);
		return ApiResponse.ok(new UploadResponse(publicUrl(path)));
	}

	// 설정한 공개 주소가 없으면 요청이 들어온 주소(프록시의 X-Forwarded-* 반영)를 쓴다
	private String publicUrl(String path) {
		if (StringUtils.hasText(properties.publicBaseUrl())) {
			return StringUtils.trimTrailingCharacter(properties.publicBaseUrl().strip(), '/') + path;
		}
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString();
	}

	public record UploadResponse(
			@Schema(example = "https://example.trycloudflare.com/uploads/store/2026/11/1f3a....jpg")
			String url) {
	}

}
