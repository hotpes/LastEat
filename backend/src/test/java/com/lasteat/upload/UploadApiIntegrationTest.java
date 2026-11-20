package com.lasteat.upload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.lasteat.TestcontainersConfiguration;
import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.member.entity.MemberRole;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// 이미지 업로드와 /uploads/** 제공. 저장 폴더는 테스트마다 임시 폴더
@SpringBootTest(properties = "lasteat.scheduler.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class UploadApiIntegrationTest {

	private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H'};

	private static final byte[] WEBP = {'R', 'I', 'F', 'F', 1, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P'};

	@TempDir
	static Path uploadDir;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtProvider jwtProvider;

	@DynamicPropertySource
	static void uploadProperties(DynamicPropertyRegistry registry) {
		registry.add("lasteat.upload.dir", () -> uploadDir.toString());
	}

	@Test
	void 이미지를_올리면_저장하고_받은_URL로_다시_내려받을_수_있다() throws Exception {
		String body = upload(MemberRole.OWNER, new MockMultipartFile("file", "../../evil.jpg", "image/jpeg", PNG),
				"STORE")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.url")
						.value(matchesPattern("^http://localhost/uploads/store/\\d{4}/\\d{2}/[0-9a-f-]{36}\\.png$")))
				.andReturn().getResponse().getContentAsString();
		String url = JsonPath.read(body, "$.data.url");
		String path = url.substring("http://localhost".length());

		// 이름은 서버가 정하고 확장자는 실제 형식(png)을 따른다
		try (Stream<Path> files = Files.walk(uploadDir)) {
			assertThat(files.filter(Files::isRegularFile).map(file -> file.getFileName().toString()))
					.anyMatch(name -> url.endsWith(name))
					.noneMatch(name -> name.contains("evil"));
		}
		mockMvc.perform(get(path))
				.andExpect(status().isOk())
				.andExpect(content().bytes(PNG));
	}

	@Test
	void webp는_메뉴_폴더에_저장한다() throws Exception {
		upload(MemberRole.OWNER, new MockMultipartFile("file", "a.webp", "image/webp", WEBP), "MENU")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.url").value(matchesPattern(".*/uploads/menu/.*\\.webp$")));
	}

	@Test
	void 이미지가_아니면_INVALID_FILE이다() throws Exception {
		MockMultipartFile[] files = {
				new MockMultipartFile("file", "a.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes()),
				new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[0]),
				new MockMultipartFile("file", "a.gif", "image/gif", "GIF89a......".getBytes()),
		};
		for (MockMultipartFile file : files) {
			upload(MemberRole.OWNER, file, "STORE")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("INVALID_FILE"));
		}
	}

	@Test
	void 용량이_10MB를_넘으면_FILE_TOO_LARGE다() throws Exception {
		byte[] large = Arrays.copyOf(PNG, (int) ImageStorage.MAX_SIZE.toBytes() + 1);
		upload(MemberRole.OWNER, new MockMultipartFile("file", "a.png", "image/png", large), "STORE")
				.andExpect(status().isContentTooLarge())
				.andExpect(jsonPath("$.error.code").value("FILE_TOO_LARGE"));
	}

	@Test
	void 파일이나_용도가_없거나_잘못되면_INVALID_INPUT이다() throws Exception {
		mockMvc.perform(multipart("/api/v1/uploads/images").param("purpose", "STORE")
						.header(HttpHeaders.AUTHORIZATION, bearer(MemberRole.OWNER)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		upload(MemberRole.OWNER, new MockMultipartFile("file", "a.png", "image/png", PNG), "PROFILE")
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		mockMvc.perform(multipart("/api/v1/uploads/images")
						.file(new MockMultipartFile("file", "a.png", "image/png", PNG))
						.header(HttpHeaders.AUTHORIZATION, bearer(MemberRole.OWNER)))
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
	}

	@Test
	void 손님_계정이나_비로그인은_올릴_수_없다() throws Exception {
		upload(MemberRole.CONSUMER, new MockMultipartFile("file", "a.png", "image/png", PNG), "STORE")
				.andExpect(status().isForbidden());
		mockMvc.perform(multipart("/api/v1/uploads/images")
						.file(new MockMultipartFile("file", "a.png", "image/png", PNG)).param("purpose", "STORE"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void 없는_파일이나_저장_폴더_밖은_404다() throws Exception {
		mockMvc.perform(get("/uploads/store/2026/11/none.png"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/uploads/../application.yaml"))
				.andExpect(status().is4xxClientError());
	}

	private ResultActions upload(MemberRole role, MockMultipartFile file, String purpose) throws Exception {
		return mockMvc.perform(multipart("/api/v1/uploads/images").file(file).param("purpose", purpose)
				.header(HttpHeaders.AUTHORIZATION, bearer(role)));
	}

	private String bearer(MemberRole role) {
		return "Bearer " + jwtProvider.issueAccessToken(new AuthMember(1L, role));
	}

}
