package com.lasteat.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.repository.ClosingDealRepository;
import com.lasteat.closingdeal.repository.ReservationRepository;
import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
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

// 사장님 메뉴 추가·수정·품절·삭제와 손님 조회 반영. 시각은 2026-11-03(화) 12:00으로 고정한다.
@SpringBootTest(properties = "lasteat.scheduler.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OwnerMenuApiIntegrationTest.FixedClockConfig.class})
class OwnerMenuApiIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 0);

	private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

	private static final String VALID_BODY = """
			{"name": "  연어 덮밥 ", "price": 13000, "description": " 구운 연어 ", "imageUrl": "",
			 "signature": true}
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
	private ClosingDealRepository closingDealRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	private Store onggi;

	private Menu stew;

	private Menu othersMenu;

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
		cleanUp();

		Member owner = saveMember("owner", MemberRole.OWNER);
		Member otherOwner = saveMember("other-owner", MemberRole.OWNER);
		ownerToken = jwtProvider.issueAccessToken(new AuthMember(owner.getId(), owner.getRole()));

		onggi = storeRepository.save(Store.builder()
				.owner(owner).name("연남 온기").category(StoreCategory.KOREAN).address("서울 동작구 상도로 1")
				.location(point(37.4972, 126.9575))
				.openTime(LocalTime.of(11, 0)).closeTime(LocalTime.of(21, 0))
				.build());
		Store others = storeRepository.save(Store.builder()
				.owner(otherOwner).name("남의 가게").category(StoreCategory.ETC).address("서울 동작구 상도로 9")
				.location(point(37.4963, 126.9575))
				.build());

		stew = menuRepository.save(Menu.builder().store(onggi).name("된장찌개").price(9000).build());
		othersMenu = menuRepository.save(Menu.builder().store(others).name("남의 메뉴").price(5000).build());
	}

	// 다른 통합 테스트는 마감할인을 지우지 않으므로 여기서 만든 것은 끝나고 지운다
	@AfterEach
	void tearDown() {
		cleanUp();
	}

	@Test
	void 메뉴를_추가하면_공백을_정리해_저장하고_손님_메뉴_목록에_대표_메뉴로_먼저_보인다() throws Exception {
		owner(post("/api/v1/owner/stores/{id}/menus", onggi.getId()), VALID_BODY)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").isNumber())
				.andExpect(jsonPath("$.data.name").value("연어 덮밥"))
				.andExpect(jsonPath("$.data.price").value(13000))
				.andExpect(jsonPath("$.data.description").value("구운 연어"))
				.andExpect(jsonPath("$.data.imageUrl").doesNotExist())
				.andExpect(jsonPath("$.data.signature").value(true))
				.andExpect(jsonPath("$.data.soldOut").value(false));

		mockMvc.perform(get("/api/v1/stores/{id}/menus", onggi.getId()))
				.andExpect(jsonPath("$.data[*].name").value(contains("연어 덮밥", "된장찌개")));
	}

	@Test
	void signature를_생략하면_대표_메뉴가_아니다() throws Exception {
		owner(post("/api/v1/owner/stores/{id}/menus", onggi.getId()), """
				{"name": "공깃밥", "price": 0}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.signature").value(false))
				.andExpect(jsonPath("$.data.price").value(0));
	}

	@Test
	void 메뉴를_수정하면_손님_조회에_반영되고_품절_여부는_그대로다() throws Exception {
		stew.changeSoldOut(true);
		menuRepository.save(stew);

		owner(put("/api/v1/owner/stores/{id}/menus/{menuId}", onggi.getId(), stew.getId()), """
				{"name": "차돌 된장찌개", "price": 11000, "signature": true}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("차돌 된장찌개"))
				.andExpect(jsonPath("$.data.soldOut").value(true));

		mockMvc.perform(get("/api/v1/stores/{id}/menus", onggi.getId()))
				.andExpect(jsonPath("$.data[0].name").value("차돌 된장찌개"))
				.andExpect(jsonPath("$.data[0].price").value(11000))
				.andExpect(jsonPath("$.data[0].description").doesNotExist())
				.andExpect(jsonPath("$.data[0].signature").value(true));
	}

	@Test
	void 품절_표시를_켜고_끌_수_있다() throws Exception {
		owner(patch("/api/v1/owner/stores/{id}/menus/{menuId}/sold-out", onggi.getId(), stew.getId()),
				"{\"soldOut\": true}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.soldOut").value(true));
		mockMvc.perform(get("/api/v1/stores/{id}/menus", onggi.getId()))
				.andExpect(jsonPath("$.data[0].soldOut").value(true));

		owner(patch("/api/v1/owner/stores/{id}/menus/{menuId}/sold-out", onggi.getId(), stew.getId()),
				"{\"soldOut\": false}")
				.andExpect(jsonPath("$.data.soldOut").value(false));
	}

	@Test
	void 삭제한_메뉴는_목록_검색_마감할인_등록에서_빠지고_지난_마감할인은_남는다() throws Exception {
		ClosingDeal pastDeal = closingDealRepository.save(ClosingDeal.builder()
				.store(onggi).menu(stew).originalPrice(9000).salePrice(6000).stock(3)
				.openAt(NOW.minusDays(1).withHour(18)).pickupEndAt(NOW.minusDays(1).withHour(21))
				.build());

		owner(delete("/api/v1/owner/stores/{id}/menus/{menuId}", onggi.getId(), stew.getId()), null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		assertThat(closingDealRepository.findById(pastDeal.getId())).isPresent();
		mockMvc.perform(get("/api/v1/stores/{id}/menus", onggi.getId()))
				.andExpect(jsonPath("$.data", hasSize(0)));
		owner(get("/api/v1/owner/stores/{id}/menus", onggi.getId()), null)
				.andExpect(jsonPath("$.data", hasSize(0)));
		mockMvc.perform(get("/api/v1/stores").param("lat", "37.4963").param("lng", "126.9575")
						.param("keyword", "된장"))
				.andExpect(jsonPath("$.data.content", hasSize(0)));
		owner(post("/api/v1/owner/stores/{id}/closing-deals", onggi.getId()), """
				{"menuId": %d, "salePrice": 5000, "stock": 2,
				 "openAt": "2026-11-03T18:00:00+09:00", "pickupEndAt": "2026-11-03T21:00:00+09:00"}
				""".formatted(stew.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MENU_NOT_FOUND"));
		owner(put("/api/v1/owner/stores/{id}/menus/{menuId}", onggi.getId(), stew.getId()), VALID_BODY)
				.andExpect(jsonPath("$.error.code").value("MENU_NOT_FOUND"));
	}

	@Test
	void 다른_가게_메뉴나_없는_메뉴는_MENU_NOT_FOUND다() throws Exception {
		owner(put("/api/v1/owner/stores/{id}/menus/{menuId}", onggi.getId(), othersMenu.getId()), VALID_BODY)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MENU_NOT_FOUND"));
		owner(delete("/api/v1/owner/stores/{id}/menus/{menuId}", onggi.getId(), 999_999), null)
				.andExpect(jsonPath("$.error.code").value("MENU_NOT_FOUND"));
		assertThat(menuRepository.findById(othersMenu.getId())).get()
				.extracting(Menu::getName).isEqualTo("남의 메뉴");
	}

	@Test
	void 남의_가게에는_메뉴를_추가할_수_없다() throws Exception {
		owner(post("/api/v1/owner/stores/{id}/menus", othersMenu.getStore().getId()), VALID_BODY)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));
	}

	@Test
	void 입력값이_잘못되면_INVALID_INPUT이다() throws Exception {
		String[] bodies = {
				"{\"name\": \"   \", \"price\": 1000}",
				"{\"name\": \"　\", \"price\": 1000}",
				"{\"name\": \"" + "가".repeat(31) + "\", \"price\": 1000}",
				"{\"name\": \"김밥\", \"price\": -1}",
				"{\"name\": \"김밥\"}",
				"{\"name\": \"김밥\", \"price\": 1000, \"description\": \"" + "가".repeat(101) + "\"}",
		};
		for (String body : bodies) {
			owner(post("/api/v1/owner/stores/{id}/menus", onggi.getId()), body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		}
		owner(patch("/api/v1/owner/stores/{id}/menus/{menuId}/sold-out", onggi.getId(), stew.getId()), "{}")
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		assertThat(menuRepository.findAllByStoreId(onggi.getId())).hasSize(1);
	}

	private ResultActions owner(MockHttpServletRequestBuilder request, String body) throws Exception {
		request.header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken);
		if (body != null) {
			request.contentType(MediaType.APPLICATION_JSON).content(body);
		}
		return mockMvc.perform(request);
	}

	private void cleanUp() {
		reservationRepository.deleteAllInBatch();
		closingDealRepository.deleteAllInBatch();
		bookmarkRepository.deleteAllInBatch();
		benefitRepository.deleteAllInBatch();
		menuRepository.deleteAllInBatch();
		storeImageRepository.deleteAllInBatch();
		storeRepository.deleteAllInBatch();
		refreshTokenRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	private Member saveMember(String providerId, MemberRole role) {
		return memberRepository.save(Member.builder()
				.provider(AuthProvider.KAKAO).providerId(providerId).nickname(providerId).role(role).build());
	}

	private static org.locationtech.jts.geom.Point point(double lat, double lng) {
		return GEOMETRY.createPoint(new Coordinate(lng, lat));
	}

}
