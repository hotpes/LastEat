package com.lasteat.registration.nts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// 국세청 사업자등록정보 진위확인 및 상태조회 (공공데이터포털 15081808).
// serviceKey: 공공데이터포털 마이페이지의 일반 인증키(Decoding). 운영 NTS_SERVICE_KEY, 로그에 남기지 않는다.
@Validated
@ConfigurationProperties("nts")
public record NtsProperties(
		@NotBlank String baseUrl,
		@NotBlank String serviceKey,
		@NotNull Duration timeout) {

	@Override
	public String toString() {
		return "NtsProperties[baseUrl=" + baseUrl + ", serviceKey=***, timeout=" + timeout + "]";
	}

}
