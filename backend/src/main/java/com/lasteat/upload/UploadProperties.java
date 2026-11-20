package com.lasteat.upload;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

// 업로드 이미지 저장 위치. 로컬 PC 서버 구성이라 디스크(컨테이너에서는 볼륨)에 저장하고 앱이 /uploads/**로 직접 내준다.
// publicBaseUrl을 비워 두면 응답 URL은 요청이 들어온 주소(터널 주소 등)를 앞에 붙인다.
@Validated
@ConfigurationProperties("lasteat.upload")
public record UploadProperties(
		@NotNull @DefaultValue("uploads") Path dir,
		String publicBaseUrl) {

	public Path root() {
		return dir.toAbsolutePath().normalize();
	}

}
