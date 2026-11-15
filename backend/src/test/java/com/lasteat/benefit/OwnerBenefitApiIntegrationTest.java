package com.lasteat.benefit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.benefit.entity.Benefit;
import com.lasteat.benefit.entity.BenefitFixture;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// 사장님 할인 공지 게시·수정·공개 전환·삭제. 시각은 2026-11-03(화) 12:00으로 고정한다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OwnerBenefitApiIntegrationTest.FixedClockConfig.class})
class OwnerBenefitApiIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 0);

	private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

	private static final String VALID_BODY = """
			{"title": "  평일 점심 10% 할인 ", "content": "식사 메뉴 10% 할인, 주류 제외", "periodText": "11월 한 달"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private StoreImageRepository storeImageRepository;

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private BenefitRepository benefitRepository;

	@Autowired
	private BookmarkRepository bookmarkRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	private Store onggi;

	private Store others;

	private Benefit othersBenefit;

	private String ownerToken;

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClockConfig {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(NOW.atZone(SEOUL).toInstant(), SEOUL);
		}

	}

	@BeforeEach
	void setUp() {
		bookmarkRepository.deleteAllInBatch();
		benefitRepository.deleteAllInBatch();
		menuRepository.deleteAllInBatch();
		storeImageRepository.deleteAllInBatch();
		storeRepository.deleteAllInBatch();
		refreshTokenRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();

		Member owner = saveMember("owner");
		Member otherOwner = saveMember("other-owner");
		ownerToken = jwtProvider.issueAccessToken(new AuthMember(owner.getId(), owner.getRole()));

		onggi = storeRepository.save(newStore(owner, "연남 온기"));
		others = storeRepository.save(newStore(otherOwner, "남의 가게"));
		othersBenefit = benefitRepository.save(BenefitFixture.notice(others, "남의 공지"));
	}

	@Test
	void 게시하면_바로_공개되고_손님_가게_상세에_보인다() throws Exception {
		owner(post("/api/v1/owner/stores/{id}/benefits", onggi.getId()), VALID_BODY)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").isNumber())
				.andExpect(jsonPath("$.data.title").value("평일 점심 10% 할인"))
				.andExpect(jsonPath("$.data.content").value("식사 메뉴 10% 할인, 주류 제외"))
				.andExpect(jsonPath("$.data.periodText").value("11월 한 달"))
				.andExpect(jsonPath("$.data.published").value(true))
				.andExpect(jsonPath("$.data.partner").value(false))
				.andExpect(jsonPath("$.data.publishedAt").value("2026-11-03T12:00:00+09:00"));

		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()))
				.andExpect(jsonPath("$.data.benefits[0].title").value("평일 점심 10% 할인"))
				.andExpect(jsonPath("$.data.benefits[0].periodText").value("11월 한 달"));
	}

	@Test
	void 공백만_있거나_화면_제한보다_길면_입력값_오류다() throws Exception {
		owner(post("/api/v1/owner/stores/{id}/benefits", onggi.getId()),
				"{\"title\": \"   \", \"content\": \"내용\", \"periodText\": \"상시\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.error.fields[0].field").value("title"))
				.andExpect(jsonPath("$.error.fields[0].reason").value("공지 제목을 입력해 주세요."));
		owner(post("/api/v1/owner/stores/{id}/benefits", onggi.getId()),
				"{\"title\": \"제목\", \"content\": \"" + "가".repeat(1001) + "\", \"periodText\": \"상시\"}")
				.andExpect(jsonPath("$.error.fields[0].field").value("content"));
		owner(post("/api/v1/owner/stores/{id}/benefits", onggi.getId()), "{\"title\": \"제목\", \"content\": \"내용\"}")
				.andExpect(jsonPath("$.error.fields[0].field").value("periodText"));
		// 한글 키보드 전각 공백만 넣어도 500이 아니라 입력값 오류다
		owner(post("/api/v1/owner/stores/{id}/benefits", onggi.getId()),
				"{\"title\": \"　　\", \"content\": \"내용\", \"periodText\": \"상시\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));

		assertThat(benefitRepository.count()).isEqualTo(1);
	}

	@Test
	void 수정해도_공개_여부와_게시_시각은_그대로다() throws Exception {
		Benefit benefit = BenefitFixture.notice(onggi, "예전 제목");
		benefit.changePublished(false);
		benefitRepository.save(benefit);

		owner(put("/api/v1/owner/stores/{id}/benefits/{benefitId}", onggi.getId(), benefit.getId()), VALID_BODY)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("평일 점심 10% 할인"))
				.andExpect(jsonPath("$.data.published").value(false))
				.andExpect(jsonPath("$.data.publishedAt").value("2026-11-01T09:30:00+09:00"));
	}

	@Test
	void 비공개로_바꾸면_손님에게_안_보이고_다시_공개할_수_있다() throws Exception {
		Benefit benefit = benefitRepository.save(BenefitFixture.notice(onggi, "공지"));
		String path = "/api/v1/owner/stores/{id}/benefits/{benefitId}/published";

		owner(patch(path, onggi.getId(), benefit.getId()), "{\"published\": false}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.published").value(false));
		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()))
				.andExpect(jsonPath("$.data.benefits", hasSize(0)));

		owner(patch(path, onggi.getId(), benefit.getId()), "{\"published\": true}")
				.andExpect(jsonPath("$.data.published").value(true));
		owner(patch(path, onggi.getId(), benefit.getId()), "{}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("published"));
	}

	@Test
	void 삭제하면_목록에서_사라진다() throws Exception {
		Benefit benefit = benefitRepository.save(BenefitFixture.notice(onggi, "공지"));

		owner(delete("/api/v1/owner/stores/{id}/benefits/{benefitId}", onggi.getId(), benefit.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").doesNotExist());

		assertThat(benefitRepository.findById(benefit.getId())).isEmpty();
	}

	@Test
	void 다른_가게의_공지는_BENEFIT_NOT_FOUND이고_남의_가게는_NOT_STORE_OWNER다() throws Exception {
		owner(put("/api/v1/owner/stores/{id}/benefits/{benefitId}", onggi.getId(), othersBenefit.getId()),
				VALID_BODY)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("BENEFIT_NOT_FOUND"));
		owner(delete("/api/v1/owner/stores/{id}/benefits/{benefitId}", onggi.getId(), othersBenefit.getId()))
				.andExpect(jsonPath("$.error.code").value("BENEFIT_NOT_FOUND"));
		owner(post("/api/v1/owner/stores/{id}/benefits", others.getId()), VALID_BODY)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));

		assertThat(benefitRepository.findById(othersBenefit.getId())).get()
				.extracting(Benefit::getTitle).isEqualTo("남의 공지");
	}

	private ResultActions owner(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken));
	}

	private ResultActions owner(MockHttpServletRequestBuilder request, String body) throws Exception {
		return owner(request.contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private Member saveMember(String providerId) {
		return memberRepository.save(Member.builder()
				.provider(AuthProvider.KAKAO).providerId(providerId).nickname(providerId).role(MemberRole.OWNER)
				.build());
	}

	private static Store newStore(Member owner, String name) {
		return Store.builder()
				.owner(owner).name(name).category(StoreCategory.KOREAN).address("서울 동작구 상도로 1")
				.location(GEOMETRY.createPoint(new Coordinate(126.9575, 37.4963)))
				.build();
	}

}
