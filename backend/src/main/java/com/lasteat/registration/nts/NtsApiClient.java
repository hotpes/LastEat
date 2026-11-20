package com.lasteat.registration.nts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// POST {baseUrl}/validate?serviceKey=... 로 사업자등록번호·개업일자·대표자 성명이 맞는지 확인한다.
// 일치하면(valid 01) 응답의 status.b_stt_cd로 계속(01)·휴업(02)·폐업(03)을 구분한다.
// 서비스 키가 URL에 들어가므로 예외 메시지(요청 URL 포함)는 로그에 남기지 않는다.
@Slf4j
public class NtsApiClient implements NtsClient {

	private static final String VALID = "01";

	private static final String ACTIVE_STATUS = "01";

	private static final String OK = "OK";

	private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final RestClient restClient;

	private final URI validateUri;

	public NtsApiClient(RestClient.Builder builder, NtsProperties properties) {
		this.restClient = builder.build();
		// 디코딩 키의 +, /, = 를 그대로 보내면 깨지므로 직접 한 번 인코딩한 URI를 쓴다
		this.validateUri = URI.create(properties.baseUrl() + "/validate?returnType=JSON&serviceKey="
				+ URLEncoder.encode(properties.serviceKey(), StandardCharsets.UTF_8));
	}

	@Override
	public BusinessVerification verify(BusinessInfo info) {
		ValidateResponse response = call(new ValidateRequest(List.of(new Business(info.businessNumber(),
				info.openDate().format(DATE), info.representativeName()))));
		if (response == null || !OK.equals(response.statusCode()) || response.data() == null
				|| response.data().isEmpty()) {
			log.warn("국세청 진위확인 응답 이상: statusCode={}", response == null ? null : response.statusCode());
			throw unavailable();
		}
		Result result = response.data().getFirst();
		if (!VALID.equals(result.valid())) {
			return BusinessVerification.NOT_MATCHED;
		}
		boolean active = result.status() != null && ACTIVE_STATUS.equals(result.status().code());
		return active ? BusinessVerification.ACTIVE : BusinessVerification.NOT_ACTIVE;
	}

	private ValidateResponse call(ValidateRequest request) {
		try {
			return restClient.post()
					.uri(validateUri)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(ValidateResponse.class);
		} catch (RestClientException e) {
			log.warn("국세청 진위확인 호출 실패: {}", e.getClass().getSimpleName());
			throw unavailable();
		}
	}

	private static BusinessException unavailable() {
		return new BusinessException(ErrorCode.BUSINESS_VERIFICATION_UNAVAILABLE);
	}

	record ValidateRequest(List<Business> businesses) {
	}

	record Business(
			@JsonProperty("b_no") String businessNumber,
			@JsonProperty("start_dt") String openDate,
			@JsonProperty("p_nm") String representativeName) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record ValidateResponse(@JsonProperty("status_code") String statusCode, List<Result> data) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Result(String valid, Status status) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Status(@JsonProperty("b_stt_cd") String code) {
	}

}
