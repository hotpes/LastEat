package com.lasteat.auth.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// 카카오 REST API로 토큰을 확인한다.
// 1) /v1/user/access_token_info: 토큰이 유효하고 우리 앱(appId)에서 발급됐는지
// 2) /v2/user/me: 회원 번호, 이메일, 닉네임
// 다른 앱에서 받은 카카오 토큰으로 로그인하지 못하도록 1)의 app_id를 반드시 비교한다.
@Slf4j
public class KakaoApiClient implements KakaoClient {

	private static final String BEARER_PREFIX = "Bearer ";

	private final RestClient restClient;

	private final long appId;

	public KakaoApiClient(RestClient.Builder builder, KakaoProperties properties) {
		this.restClient = builder.baseUrl(properties.baseUrl()).build();
		this.appId = properties.appId();
	}

	@Override
	public KakaoUser fetchUser(String kakaoAccessToken) {
		try {
			TokenInfo tokenInfo = get("/v1/user/access_token_info", kakaoAccessToken, TokenInfo.class);
			if (tokenInfo == null || tokenInfo.appId() == null || tokenInfo.appId() != appId) {
				log.warn("다른 앱에서 발급된 카카오 토큰: appId={}", tokenInfo == null ? null : tokenInfo.appId());
				throw failed();
			}
			UserResponse user = get("/v2/user/me", kakaoAccessToken, UserResponse.class);
			if (user == null || user.id() == null) {
				throw failed();
			}
			return user.toKakaoUser();
		} catch (RestClientException e) {
			log.warn("카카오 토큰 확인 실패: {}", e.getMessage());
			throw failed();
		}
	}

	private <T> T get(String path, String kakaoAccessToken, Class<T> type) {
		return restClient.get()
				.uri(path)
				.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + kakaoAccessToken)
				.retrieve()
				.body(type);
	}

	private static BusinessException failed() {
		return new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record TokenInfo(@JsonProperty("app_id") Long appId) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record UserResponse(Long id, @JsonProperty("kakao_account") Account kakaoAccount) {

		KakaoUser toKakaoUser() {
			if (kakaoAccount == null) {
				return new KakaoUser(id.toString(), null, null);
			}
			String nickname = kakaoAccount.profile() == null ? null : kakaoAccount.profile().nickname();
			return new KakaoUser(id.toString(), kakaoAccount.email(), nickname);
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Account(String email, Profile profile) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Profile(String nickname) {
	}

}
