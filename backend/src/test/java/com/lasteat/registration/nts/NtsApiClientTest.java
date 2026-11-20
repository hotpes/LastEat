package com.lasteat.registration.nts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class NtsApiClientTest {

	private static final String BASE_URL = "https://api.nts.test/v1";

	// 디코딩 키에는 +, /, = 가 들어 있다
	private static final String SERVICE_KEY = "ab+c/d==";

	private static final String VALIDATE_URL = BASE_URL + "/validate?returnType=JSON&serviceKey=ab%2Bc%2Fd%3D%3D";

	private static final BusinessInfo INFO = new BusinessInfo("1234567890", LocalDate.of(2021, 3, 5), "홍길동");

	private MockRestServiceServer server;

	private NtsApiClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new NtsApiClient(builder, new NtsProperties(BASE_URL, SERVICE_KEY, Duration.ofSeconds(3)));
	}

	@Test
	void 일치하고_계속사업자면_ACTIVE다() {
		server.expect(requestTo(VALIDATE_URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("""
						{"businesses": [{"b_no": "1234567890", "start_dt": "20210305", "p_nm": "홍길동"}]}
						"""))
				.andRespond(withSuccess(validResponse("01"), MediaType.APPLICATION_JSON));

		assertThat(client.verify(INFO)).isEqualTo(BusinessVerification.ACTIVE);
		server.verify();
	}

	@Test
	void 일치하지만_휴업이나_폐업이면_NOT_ACTIVE다() {
		server.expect(requestTo(VALIDATE_URL))
				.andRespond(withSuccess(validResponse("03"), MediaType.APPLICATION_JSON));

		assertThat(client.verify(INFO)).isEqualTo(BusinessVerification.NOT_ACTIVE);
	}

	@Test
	void 일치하지_않으면_NOT_MATCHED다() {
		// 실제 국세청 응답 형식 (2026-11-20 확인)
		server.expect(requestTo(VALIDATE_URL))
				.andRespond(withSuccess("""
						{"request_cnt": 1, "status_code": "OK", "data": [{"b_no": "1234567890", "valid": "02",
						 "valid_msg": "확인할 수 없습니다.", "request_param": {"b_no": "1234567890"}}]}
						""", MediaType.APPLICATION_JSON));

		assertThat(client.verify(INFO)).isEqualTo(BusinessVerification.NOT_MATCHED);
	}

	@Test
	void 국세청이_오류를_돌려주면_UNAVAILABLE이다() {
		server.expect(requestTo(VALIDATE_URL))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
						.body("{\"code\": -4, \"msg\": \"등록되지 않은 인증키 입니다.\"}"));

		assertUnavailable();
	}

	@Test
	void 응답에_결과가_없으면_UNAVAILABLE이다() {
		server.expect(requestTo(VALIDATE_URL))
				.andRespond(withSuccess("{\"status_code\": \"OK\", \"data\": []}", MediaType.APPLICATION_JSON));

		assertUnavailable();
	}

	@Test
	void 설정의_서비스_키는_문자열로_찍혀도_가려진다() {
		assertThat(new NtsProperties(BASE_URL, SERVICE_KEY, Duration.ofSeconds(3)).toString())
				.doesNotContain(SERVICE_KEY);
	}

	private void assertUnavailable() {
		assertThatThrownBy(() -> client.verify(INFO))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.BUSINESS_VERIFICATION_UNAVAILABLE);
	}

	private static String validResponse(String statusCode) {
		return """
				{"request_cnt": 1, "valid_cnt": 1, "status_code": "OK", "data": [{"b_no": "1234567890",
				 "valid": "01", "status": {"b_no": "1234567890", "b_stt": "계속사업자", "b_stt_cd": "%s"}}]}
				""".formatted(statusCode);
	}

}
