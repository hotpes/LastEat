package com.lasteat.global.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// 실제 SecurityConfig로 띄워서 막힌 요청의 응답 형식과 공개 경로를 확인한다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SecurityErrorHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 토큰_없이_막힌_주소에_오면_401_공통_형식으로_응답한다() throws Exception {
		mockMvc.perform(get("/api/v1/members/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
				.andExpect(jsonPath("$.error.message").value("로그인이 필요합니다."));
	}

	@Test
	@WithMockUser
	void 로그인한_상태로_없는_주소에_오면_404_공통_형식으로_응답한다() throws Exception {
		mockMvc.perform(get("/api/v1/none"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void 헬스_체크와_API_문서는_로그인_없이_열린다() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("LastEat API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));

		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

}
