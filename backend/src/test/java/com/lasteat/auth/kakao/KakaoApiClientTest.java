package com.lasteat.auth.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KakaoApiClientTest {

	private static final String BASE_URL = "https://kapi.kakao.test";

	private static final long APP_ID = 1234L;

	private MockRestServiceServer server;

	private KakaoApiClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new KakaoApiClient(builder, new KakaoProperties(BASE_URL, APP_ID, Duration.ofSeconds(3)));
	}

	@Test
	void 우리_앱_토큰이면_회원_정보를_돌려준다() {
		expectTokenInfo("{\"id\": 4001, \"app_id\": 1234, \"expires_in\": 7199}");
		server.expect(requestTo(BASE_URL + "/v2/user/me"))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer kakao-token"))
				.andRespond(withSuccess("""
						{"id": 4001, "connected_at": "2026-11-03T00:00:00Z",
						 "kakao_account": {"email": "user@kakao.com", "profile": {"nickname": "맛집탐험가"}}}
						""", MediaType.APPLICATION_JSON));

		KakaoUser user = client.fetchUser("kakao-token");

		assertThat(user).isEqualTo(new KakaoUser("4001", "user@kakao.com", "맛집탐험가"));
		server.verify();
	}

	@Test
	void 이메일과_닉네임_동의가_없어도_회원_번호만으로_확인된다() {
		expectTokenInfo("{\"id\": 4001, \"app_id\": 1234}");
		server.expect(requestTo(BASE_URL + "/v2/user/me"))
				.andRespond(withSuccess("{\"id\": 4001}", MediaType.APPLICATION_JSON));

		assertThat(client.fetchUser("kakao-token")).isEqualTo(new KakaoUser("4001", null, null));
	}

	@Test
	void 다른_앱에서_발급된_토큰이면_실패하고_회원_정보를_조회하지_않는다() {
		expectTokenInfo("{\"id\": 4001, \"app_id\": 9999}");

		assertKakaoFailed();
		server.verify();
	}

	@Test
	void 카카오가_토큰을_거절하면_실패한다() {
		server.expect(requestTo(BASE_URL + "/v1/user/access_token_info"))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED)
						.contentType(MediaType.APPLICATION_JSON)
						.body("{\"msg\": \"this access token does not exist\", \"code\": -401}"));

		assertKakaoFailed();
	}

	@Test
	void 카카오_서버_오류도_로그인_실패로_응답한다() {
		server.expect(requestTo(BASE_URL + "/v1/user/access_token_info"))
				.andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

		assertKakaoFailed();
	}

	private void expectTokenInfo(String body) {
		server.expect(requestTo(BASE_URL + "/v1/user/access_token_info"))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer kakao-token"))
				.andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
	}

	private void assertKakaoFailed() {
		assertThatThrownBy(() -> client.fetchUser("kakao-token"))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.KAKAO_AUTH_FAILED);
	}

}
