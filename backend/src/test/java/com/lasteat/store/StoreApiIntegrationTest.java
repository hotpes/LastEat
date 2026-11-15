package com.lasteat.store;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.entity.StoreImage;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// 실제 MySQL 공간 함수로 반경 검색·필터·상세·저장을 확인한다.
// 시각은 2026-11-03(화) 12:00으로 고정한다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, StoreApiIntegrationTest.FixedClockConfig.class})
class StoreApiIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 0);

	// 숭실대 근처 기준 좌표
	private static final double LAT = 37.4963;

	private static final double LNG = 126.9575;

	private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

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

	private Store momo;

	private Store pocha;

	private Store far;

	private String token;

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

		// 북쪽 약 100m, 11~21시, 15~17시 쉬는 시간, 월요일 휴무, 공개 중인 할인 공지
		onggi = storeRepository.save(Store.builder()
				.name("연남 온기").category(StoreCategory.KOREAN).address("서울 동작구 상도로 1")
				.location(point(LAT + 0.0009, LNG))
				.openTime(LocalTime.of(11, 0)).closeTime(LocalTime.of(21, 0))
				.breakStart(LocalTime.of(15, 0)).breakEnd(LocalTime.of(17, 0))
				.closedDays(Set.of(DayOfWeek.MONDAY))
				.tags(List.of("혼밥 좋아요", "포장 가능"))
				.build());
		// 남쪽 약 200m, 18~02시 (지금은 영업 종료)
		pocha = storeRepository.save(Store.builder()
				.name("늦은밤 포차").category(StoreCategory.KOREAN).address("서울 동작구 상도로 2")
				.location(point(LAT - 0.0018, LNG))
				.openTime(LocalTime.of(18, 0)).closeTime(LocalTime.of(2, 0))
				.build());
		// 동쪽 약 300m, 08~22시, 비공개 할인 공지만 있음
		momo = storeRepository.save(Store.builder()
				.name("모모").category(StoreCategory.CAFE).address("서울 동작구 상도로 3")
				.location(point(LAT, LNG + 0.0034))
				.openTime(LocalTime.of(8, 0)).closeTime(LocalTime.of(22, 0))
				.build());
		// 북쪽 약 2km
		far = storeRepository.save(Store.builder()
				.name("멀리 식당").category(StoreCategory.ETC).address("서울 동작구 상도로 4")
				.location(point(LAT + 0.018, LNG))
				.openTime(LocalTime.of(8, 0)).closeTime(LocalTime.of(22, 0))
				.build());

		storeImageRepository.save(StoreImage.builder().store(onggi).imageUrl("https://img/onggi-2.jpg").sortOrder(1)
				.build());
		storeImageRepository.save(StoreImage.builder().store(onggi).imageUrl("https://img/onggi-1.jpg").sortOrder(0)
				.build());
		menuRepository.save(Menu.builder().store(onggi).name("된장찌개").price(9000).build());
		menuRepository.save(Menu.builder().store(onggi).name("연어 아보카도 덮밥").price(15000).signature(true).build());
		menuRepository.save(Menu.builder().store(momo).name("아메리카노").price(3000).signature(true).build());

		Benefit visible = BenefitFixture.notice(onggi, "식사 금액 10% 할인");
		visible.changePartner(true);
		benefitRepository.save(visible);
		Benefit hidden = BenefitFixture.notice(momo, "비공개 공지");
		hidden.changePublished(false);
		benefitRepository.save(hidden);

		Member member = memberRepository.save(Member.builder()
				.provider(AuthProvider.KAKAO).providerId("store-test").nickname("회원").role(MemberRole.CONSUMER)
				.build());
		token = jwtProvider.issueAccessToken(new AuthMember(member.getId(), member.getRole()));
	}

	@Test
	void 기본_반경_500m_안의_가게를_가까운_순으로_보여준다() throws Exception {
		search("")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[*].name").value(contains("연남 온기", "늦은밤 포차", "모모")))
				.andExpect(jsonPath("$.data.totalElements").value(3))
				.andExpect(jsonPath("$.data.hasNext").value(false))
				.andExpect(jsonPath("$.data.content[0].distance").value(allOf(greaterThanOrEqualTo(95),
						lessThanOrEqualTo(105))))
				.andExpect(jsonPath("$.data.content[0].lat").value(LAT + 0.0009))
				.andExpect(jsonPath("$.data.content[0].lng").value(LNG))
				.andExpect(jsonPath("$.data.content[0].thumbnailUrl").value("https://img/onggi-1.jpg"))
				.andExpect(jsonPath("$.data.content[0].signatureMenuName").value("연어 아보카도 덮밥"))
				.andExpect(jsonPath("$.data.content[0].mainBenefit.title").value("식사 금액 10% 할인"))
				.andExpect(jsonPath("$.data.content[0].businessStatus").value("OPEN"))
				.andExpect(jsonPath("$.data.content[0].bookmarked").value(false))
				.andExpect(jsonPath("$.data.content[1].businessStatus").value("CLOSED"))
				.andExpect(jsonPath("$.data.content[2].mainBenefit").doesNotExist());
	}

	@Test
	void 반경을_넓히면_먼_가게도_나온다() throws Exception {
		search("&radius=3000")
				.andExpect(jsonPath("$.data.totalElements").value(4))
				.andExpect(jsonPath("$.data.content[3].name").value("멀리 식당"));
	}

	@Test
	void 지금_영업_중인_곳만_거른다() throws Exception {
		search("&openNow=true")
				.andExpect(jsonPath("$.data.content[*].name").value(contains("연남 온기", "모모")))
				.andExpect(jsonPath("$.data.totalElements").value(2));
	}

	@Test
	void 카테고리로_거른다() throws Exception {
		search("&category=CAFE").andExpect(jsonPath("$.data.content[*].name").value(contains("모모")));
		search("&category=KOREAN,CAFE").andExpect(jsonPath("$.data.totalElements").value(3));
	}

	@Test
	void 가게_이름_메뉴_이름_카테고리_이름으로_검색한다() throws Exception {
		search("&keyword=온기").andExpect(jsonPath("$.data.content[*].name").value(contains("연남 온기")));
		search("&keyword=아메리카노").andExpect(jsonPath("$.data.content[*].name").value(contains("모모")));
		search("&keyword=카페").andExpect(jsonPath("$.data.content[*].name").value(contains("모모")));
		search("&keyword=%25%25").andExpect(jsonPath("$.data.totalElements").value(0));
	}

	@Test
	void 공개_중인_할인_공지가_있는_곳만_거른다() throws Exception {
		search("&hasBenefit=true")
				.andExpect(jsonPath("$.data.content[*].name").value(contains("연남 온기")));
	}

	@Test
	void 페이지로_나눠_보여준다() throws Exception {
		search("&size=2")
				.andExpect(jsonPath("$.data.content", hasSize(2)))
				.andExpect(jsonPath("$.data.hasNext").value(true))
				.andExpect(jsonPath("$.data.totalElements").value(3));
		search("&size=2&page=1")
				.andExpect(jsonPath("$.data.content[*].name").value(contains("모모")))
				.andExpect(jsonPath("$.data.hasNext").value(false));
	}

	@Test
	void 검색_조건이_틀리면_입력값_오류다() throws Exception {
		mockMvc.perform(get("/api/v1/stores").param("lng", String.valueOf(LNG)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("lat"));
		search("&radius=3001").andExpect(jsonPath("$.error.fields[0].field").value("radius"));
		search("&keyword=a").andExpect(jsonPath("$.error.fields[0].field").value("keyword"));
		search("&category=PIZZA")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("category"))
				.andExpect(jsonPath("$.error.fields[0].reason").value("형식이 올바르지 않습니다."));
	}

	@Test
	void 가게_상세에_사진_영업시간_대표_메뉴_혜택_요약이_나온다() throws Exception {
		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("연남 온기"))
				.andExpect(jsonPath("$.data.distance").doesNotExist())
				.andExpect(jsonPath("$.data.imageUrls").value(contains("https://img/onggi-1.jpg",
						"https://img/onggi-2.jpg")))
				.andExpect(jsonPath("$.data.tags").value(contains("혼밥 좋아요", "포장 가능")))
				.andExpect(jsonPath("$.data.businessStatus").value("OPEN"))
				.andExpect(jsonPath("$.data.businessHours.openTime").value("11:00"))
				.andExpect(jsonPath("$.data.businessHours.breakStart").value("15:00"))
				.andExpect(jsonPath("$.data.businessHours.closedDays").value(contains("MON")))
				.andExpect(jsonPath("$.data.signatureMenus[*].name").value(contains("연어 아보카도 덮밥")))
				.andExpect(jsonPath("$.data.menuCount").value(2))
				.andExpect(jsonPath("$.data.benefits[0].partner").value(true))
				.andExpect(jsonPath("$.data.benefits[0].title").value("식사 금액 10% 할인"))
				.andExpect(jsonPath("$.data.benefits[0].periodText").value("11월 한 달"))
				.andExpect(jsonPath("$.data.benefits[0].summary").doesNotExist())
				.andExpect(jsonPath("$.data.bookmarked").value(false));
	}

	@Test
	void 상세에_좌표를_보내면_거리를_계산하고_하나만_보내면_입력값_오류다() throws Exception {
		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId())
						.param("lat", String.valueOf(LAT)).param("lng", String.valueOf(LNG)))
				.andExpect(jsonPath("$.data.distance").value(allOf(greaterThanOrEqualTo(95), lessThanOrEqualTo(105))));
		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()).param("lat", String.valueOf(LAT)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
	}

	@Test
	void 없는_가게는_STORE_NOT_FOUND다() throws Exception {
		mockMvc.perform(get("/api/v1/stores/{id}", 999_999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
		mockMvc.perform(get("/api/v1/stores/{id}/menus", 999_999))
				.andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
	}

	@Test
	void 메뉴는_대표_메뉴가_먼저다() throws Exception {
		mockMvc.perform(get("/api/v1/stores/{id}/menus", onggi.getId()))
				.andExpect(jsonPath("$.data[*].name").value(contains("연어 아보카도 덮밥", "된장찌개")))
				.andExpect(jsonPath("$.data[0].signature").value(true));
	}

	@Test
	void 저장은_로그인이_필요하고_여러_번_눌러도_한_번만_저장된다() throws Exception {
		mockMvc.perform(post("/api/v1/stores/{id}/bookmark", onggi.getId()))
				.andExpect(status().isUnauthorized());

		withToken(post("/api/v1/stores/{id}/bookmark", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.bookmarked").value(true));
		withToken(post("/api/v1/stores/{id}/bookmark", onggi.getId())).andExpect(status().isOk());

		org.assertj.core.api.Assertions.assertThat(bookmarkRepository.count()).isEqualTo(1);
		withToken(get("/api/v1/stores?lat=" + LAT + "&lng=" + LNG))
				.andExpect(jsonPath("$.data.content[0].bookmarked").value(true))
				.andExpect(jsonPath("$.data.content[1].bookmarked").value(false));
		withToken(get("/api/v1/stores/{id}", onggi.getId())).andExpect(jsonPath("$.data.bookmarked").value(true));
	}

	@Test
	void 저장_취소는_저장하지_않은_가게여도_성공한다() throws Exception {
		withToken(post("/api/v1/stores/{id}/bookmark", onggi.getId()));

		withToken(delete("/api/v1/stores/{id}/bookmark", onggi.getId()))
				.andExpect(jsonPath("$.data.bookmarked").value(false));
		withToken(delete("/api/v1/stores/{id}/bookmark", onggi.getId())).andExpect(status().isOk());
		org.assertj.core.api.Assertions.assertThat(bookmarkRepository.count()).isZero();
	}

	@Test
	void 없는_가게는_저장할_수_없다() throws Exception {
		withToken(post("/api/v1/stores/{id}/bookmark", 999_999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
	}

	@Test
	void 내_저장_목록은_최근_저장_순이고_거리순과_필터를_지원한다() throws Exception {
		withToken(post("/api/v1/stores/{id}/bookmark", momo.getId()));
		withToken(post("/api/v1/stores/{id}/bookmark", far.getId()));
		withToken(post("/api/v1/stores/{id}/bookmark", onggi.getId()));

		// 같은 시각(고정 시계)에 저장했으므로 최근 순은 나중에 저장한 것부터 (id 역순)
		withToken(get("/api/v1/members/me/bookmarks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[*].name").value(contains("연남 온기", "멀리 식당", "모모")))
				.andExpect(jsonPath("$.data.content[0].bookmarked").value(true))
				.andExpect(jsonPath("$.data.content[0].distance").doesNotExist())
				.andExpect(jsonPath("$.data.content[0].bookmarkedAt").value("2026-11-03T12:00:00+09:00"));
		withToken(get("/api/v1/members/me/bookmarks?sort=DISTANCE&lat=" + LAT + "&lng=" + LNG))
				.andExpect(jsonPath("$.data.content[*].name").value(contains("연남 온기", "모모", "멀리 식당")))
				.andExpect(jsonPath("$.data.content[0].distance", notNullValue()));
		withToken(get("/api/v1/members/me/bookmarks?category=CAFE"))
				.andExpect(jsonPath("$.data.content[*].name").value(contains("모모")));
		withToken(get("/api/v1/members/me/bookmarks?sort=DISTANCE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
	}

	private ResultActions search(String query) throws Exception {
		return mockMvc.perform(get("/api/v1/stores?lat=" + LAT + "&lng=" + LNG + query));
	}

	private ResultActions withToken(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
	}

	private static org.locationtech.jts.geom.Point point(double lat, double lng) {
		return GEOMETRY.createPoint(new Coordinate(lng, lat));
	}

}
