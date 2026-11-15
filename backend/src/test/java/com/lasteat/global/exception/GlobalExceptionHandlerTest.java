package com.lasteat.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 보안 필터는 빼고 예외 → 공통 응답 변환만 확인한다. 보안 응답은 SecurityErrorHandlerTest에서 본다.
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandlerTest.TestController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 비즈니스_예외는_에러_코드의_상태와_문구로_응답한다() throws Exception {
		mockMvc.perform(get("/test/business"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"))
				.andExpect(jsonPath("$.error.message").value("가게를 찾을 수 없습니다."))
				.andExpect(jsonPath("$.error.fields").doesNotExist());
	}

	@Test
	void 요청_본문_검증에_실패하면_필드별_사유를_담는다() throws Exception {
		mockMvc.perform(post("/test/body")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\": \"" + "가".repeat(31) + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.error.fields[0].field").value("title"))
				.andExpect(jsonPath("$.error.fields[0].reason").value("30자 이하로 입력해 주세요."));
	}

	@Test
	void 쿼리_파라미터_검증에_실패하면_파라미터_이름을_담는다() throws Exception {
		mockMvc.perform(get("/test/param").param("size", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.error.fields[0].field").value("size"))
				.andExpect(jsonPath("$.error.fields[0].reason").value("100 이하로 입력해 주세요."));
	}

	@Test
	void 필수_파라미터가_없거나_형식이_틀리면_입력값_오류다() throws Exception {
		mockMvc.perform(get("/test/param"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("size"))
				.andExpect(jsonPath("$.error.fields[0].reason").value("필수 값입니다."));

		mockMvc.perform(get("/test/path/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("id"))
				.andExpect(jsonPath("$.error.fields[0].reason").value("형식이 올바르지 않습니다."));
	}

	@Test
	void JSON을_읽을_수_없으면_입력값_오류다() throws Exception {
		mockMvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
	}

	@Test
	void 없는_주소와_지원하지_않는_메서드도_공통_형식으로_응답한다() throws Exception {
		mockMvc.perform(get("/test/none"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

		mockMvc.perform(post("/test/business"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	void 예상하지_못한_예외는_내부_메시지를_숨기고_서버_오류로_응답한다() throws Exception {
		mockMvc.perform(get("/test/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
				.andExpect(jsonPath("$.error.message").value("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
	}

	record TitleRequest(@NotBlank @Size(max = 30, message = "30자 이하로 입력해 주세요.") String title) {
	}

	@RestController
	static class TestController {

		@GetMapping("/test/business")
		void business() {
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}

		@PostMapping("/test/body")
		void body(@Valid @RequestBody TitleRequest request) {
		}

		@GetMapping("/test/param")
		void param(@RequestParam @Max(value = 100, message = "100 이하로 입력해 주세요.") int size) {
		}

		@GetMapping("/test/path/{id}")
		void path(@PathVariable Long id) {
		}

		@GetMapping("/test/unexpected")
		void unexpected() {
			throw new IllegalStateException("내부 정보 db=secret");
		}

	}

}
