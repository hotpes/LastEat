package com.lasteat.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.auth.jwt.JwtProperties;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.auth.kakao.KakaoClient;
import com.lasteat.auth.kakao.KakaoUser;
import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.auth.token.RefreshTokens;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.member.repository.MemberRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 카카오 API만 가짜로 두고, 보안 필터·DB까지 실제로 거쳐 로그인 흐름을 확인한다.
// 테스트끼리 DB를 공유하므로 카카오 회원 번호를 테스트마다 다르게 쓴다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private JwtProperties jwtProperties;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@MockitoBean
	private KakaoClient kakaoClient;

	@Test
	void 처음_로그인하면_손님으로_가입되고_다시_로그인하면_같은_회원이다() throws Exception {
		given(kakaoClient.fetchUser("token-1")).willReturn(new KakaoUser("1001", "a@kakao.com", "맛집탐험가"));

		JsonNode first = data(login("token-1", "fcm-1")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.newMember").value(true))
				.andExpect(jsonPath("$.data.member.nickname").value("맛집탐험가"))
				.andExpect(jsonPath("$.data.member.role").value("CONSUMER")));
		JsonNode second = data(login("token-1", null)
				.andExpect(jsonPath("$.data.newMember").value(false)));

		long memberId = first.get("member").get("id").asLong();
		assertThat(second.get("member").get("id").asLong()).isEqualTo(memberId);
		Member member = memberRepository.findById(memberId).orElseThrow();
		assertThat(member.getProvider()).isEqualTo(AuthProvider.KAKAO);
		assertThat(member.getEmail()).isEqualTo("a@kakao.com");
		assertThat(member.getFcmToken()).isEqualTo("fcm-1");
	}

	@Test
	void refreshToken은_해시로만_저장된다() throws Exception {
		given(kakaoClient.fetchUser("token-2")).willReturn(new KakaoUser("1002", null, null));

		String refreshToken = data(login("token-2", null)).get("refreshToken").asString();

		assertThat(refreshTokenRepository.findByTokenHash(refreshToken)).isEmpty();
		assertThat(refreshTokenRepository.findByTokenHash(RefreshTokens.hash(refreshToken))).isPresent();
	}

	@Test
	void 받은_accessToken으로_로그인이_필요한_주소에_들어갈_수_있다() throws Exception {
		given(kakaoClient.fetchUser("token-3")).willReturn(new KakaoUser("1003", null, null));
		String accessToken = data(login("token-3", null)).get("accessToken").asString();

		// 아직 컨트롤러가 없는 주소: 인증을 통과하면 404, 막히면 401
		mockMvc.perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/v1/members/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	void 재발급하면_새_토큰을_주고_이전_refreshToken은_다시_쓸_수_없다() throws Exception {
		given(kakaoClient.fetchUser("token-4")).willReturn(new KakaoUser("1004", null, null));
		String oldRefreshToken = data(login("token-4", null)).get("refreshToken").asString();

		JsonNode renewed = data(refresh(oldRefreshToken).andExpect(status().isOk()));

		assertThat(renewed.get("refreshToken").asString()).isNotEqualTo(oldRefreshToken);
		assertThat(renewed.get("accessToken").asString()).isNotBlank();
		refresh(oldRefreshToken)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
		refresh(renewed.get("refreshToken").asString()).andExpect(status().isOk());
	}

	@Test
	void 로그아웃하면_그_refreshToken으로_재발급할_수_없다() throws Exception {
		given(kakaoClient.fetchUser("token-5")).willReturn(new KakaoUser("1005", null, null));
		String refreshToken = data(login("token-5", null)).get("refreshToken").asString();

		postJson("/api/v1/auth/logout", refreshBody(refreshToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		refresh(refreshToken).andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
		postJson("/api/v1/auth/logout", refreshBody(refreshToken)).andExpect(status().isOk());
	}

	@Test
	void 만료되거나_잘못된_accessToken은_이유별_코드로_401이다() throws Exception {
		Instant twoHoursAgo = Instant.now().minus(Duration.ofHours(2));
		JwtProvider pastProvider = new JwtProvider(jwtProperties, Clock.fixed(twoHoursAgo, ZoneId.of("Asia/Seoul")));
		String expired = pastProvider.issueAccessToken(new AuthMember(1L, MemberRole.CONSUMER));

		mockMvc.perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("EXPIRED_TOKEN"));
		mockMvc.perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
	}

	@Test
	void 사장님_주소는_사장님_역할만_들어갈_수_있다() throws Exception {
		String consumer = jwtProvider.issueAccessToken(new AuthMember(1L, MemberRole.CONSUMER));
		String owner = jwtProvider.issueAccessToken(new AuthMember(2L, MemberRole.OWNER));

		mockMvc.perform(get("/api/v1/owner/stores").header(HttpHeaders.AUTHORIZATION, "Bearer " + consumer))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
		mockMvc.perform(get("/api/v1/owner/stores").header(HttpHeaders.AUTHORIZATION, "Bearer " + owner))
				.andExpect(status().isOk());
	}

	@Test
	void 카카오_확인에_실패하면_KAKAO_AUTH_FAILED다() throws Exception {
		given(kakaoClient.fetchUser("bad-token")).willThrow(new BusinessException(ErrorCode.KAKAO_AUTH_FAILED));

		login("bad-token", null)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("KAKAO_AUTH_FAILED"))
				.andExpect(jsonPath("$.error.message").value("카카오 로그인에 실패했습니다."));
	}

	@Test
	void 카카오_토큰이_비어_있으면_입력값_오류다() throws Exception {
		postJson("/api/v1/auth/kakao", "{\"kakaoAccessToken\": \" \"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.error.fields[0].field").value("kakaoAccessToken"));
	}

	private ResultActions login(String kakaoToken, String fcmToken) throws Exception {
		String fcm = fcmToken == null ? "null" : "\"" + fcmToken + "\"";
		return postJson("/api/v1/auth/kakao", "{\"kakaoAccessToken\": \"" + kakaoToken + "\", \"fcmToken\": " + fcm + "}");
	}

	private ResultActions refresh(String refreshToken) throws Exception {
		return postJson("/api/v1/auth/refresh", refreshBody(refreshToken));
	}

	private static String refreshBody(String refreshToken) {
		return "{\"refreshToken\": \"" + refreshToken + "\"}";
	}

	private ResultActions postJson(String path, String body) throws Exception {
		return mockMvc.perform(post(path)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	private JsonNode data(ResultActions result) throws Exception {
		String body = result.andReturn().getResponse().getContentAsString();
		return jsonMapper.readTree(body).get("data");
	}

}
