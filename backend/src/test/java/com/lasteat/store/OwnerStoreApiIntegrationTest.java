package com.lasteat.store;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.lasteat.store.entity.BusinessStatus;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.entity.StoreImage;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

// 사장님 조회 API (사장님 앱 OwnerReadApi가 부르는 4개). 시각은 2026-11-03(화) 12:00으로 고정한다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OwnerStoreApiIntegrationTest.FixedClockConfig.class})
class OwnerStoreApiIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 0);

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

	private Store others;

	private String ownerToken;

	private String otherOwnerToken;

	private String consumerToken;

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

		Member owner = saveMember("owner", MemberRole.OWNER);
		Member otherOwner = saveMember("other-owner", MemberRole.OWNER);
		Member consumer = saveMember("consumer", MemberRole.CONSUMER);
		ownerToken = tokenOf(owner);
		otherOwnerToken = tokenOf(otherOwner);
		consumerToken = tokenOf(consumer);

		onggi = storeRepository.save(Store.builder()
				.owner(owner).name("연남 온기").category(StoreCategory.KOREAN).address("서울 동작구 상도로 1")
				.location(point(37.4972, 126.9575))
				.openTime(LocalTime.of(11, 0)).closeTime(LocalTime.of(21, 0))
				.closedDays(Set.of(DayOfWeek.MONDAY))
				.build());
		// 영업시간 미등록
		momo = storeRepository.save(Store.builder()
				.owner(owner).name("모모").category(StoreCategory.CAFE).address("서울 동작구 상도로 3")
				.location(point(37.4963, 126.9609))
				.build());
		others = storeRepository.save(Store.builder()
				.owner(otherOwner).name("남의 가게").category(StoreCategory.ETC).address("서울 동작구 상도로 9")
				.location(point(37.4963, 126.9575))
				.build());

		storeImageRepository.save(StoreImage.builder().store(onggi).imageUrl("https://img/onggi-1.jpg").sortOrder(0)
				.build());
		menuRepository.save(Menu.builder().store(onggi).name("된장찌개").price(9000).build());
		menuRepository.save(Menu.builder().store(onggi).name("연어 아보카도 덮밥").price(15000).signature(true).build());

		benefitRepository.save(BenefitFixture.notice(onggi, "평일 점심 10% 할인"));
		Benefit hidden = BenefitFixture.notice(onggi, "지난 공지", BenefitFixture.PUBLISHED_AT.plusDays(1));
		hidden.changePublished(false);
		benefitRepository.save(hidden);
		benefitRepository.save(BenefitFixture.notice(others, "남의 공지"));
	}

	@Test
	void 내_가게_목록은_내_가게만_등록_순으로_보여준다() throws Exception {
		owner(get("/api/v1/owner/stores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].name").value(contains("연남 온기", "모모")))
				.andExpect(jsonPath("$.data[0].address").value("서울 동작구 상도로 1"))
				.andExpect(jsonPath("$.data[0].thumbnailUrl").value("https://img/onggi-1.jpg"))
				.andExpect(jsonPath("$.data[0].businessStatus").value("OPEN"))
				.andExpect(jsonPath("$.data[1].thumbnailUrl").doesNotExist())
				.andExpect(jsonPath("$.data[1].businessStatus").value("CLOSED"));
	}

	@Test
	void 가게가_없는_사장님은_빈_목록이다() throws Exception {
		Member lonely = saveMember("lonely", MemberRole.OWNER);
		mockMvc.perform(get("/api/v1/owner/stores").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOf(lonely)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(0)));
	}

	@Test
	void 가게_정보는_가게_상세_형식에_오늘_직접_정한_영업_상태를_더한다() throws Exception {
		onggi.overrideBusinessStatus(BusinessStatus.BREAK, LocalDate.of(2026, 11, 3));
		storeRepository.save(onggi);

		owner(get("/api/v1/owner/stores/{id}", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(onggi.getId()))
				.andExpect(jsonPath("$.data.name").value("연남 온기"))
				.andExpect(jsonPath("$.data.category").value("KOREAN"))
				.andExpect(jsonPath("$.data.imageUrls").value(contains("https://img/onggi-1.jpg")))
				.andExpect(jsonPath("$.data.businessHours.openTime").value("11:00"))
				.andExpect(jsonPath("$.data.businessHours.closedDays").value(contains("MON")))
				.andExpect(jsonPath("$.data.businessStatus").value("BREAK"))
				.andExpect(jsonPath("$.data.businessStatusOverride").value("BREAK"))
				.andExpect(jsonPath("$.data.menuCount").value(2))
				.andExpect(jsonPath("$.data.benefits", hasSize(1)))
				.andExpect(jsonPath("$.data.distance").doesNotExist())
				.andExpect(jsonPath("$.data.bookmarked").doesNotExist());
	}

	@Test
	void 어제_정한_영업_상태는_무시하고_영업시간이_없으면_영업시간도_비어_있다() throws Exception {
		momo.overrideBusinessStatus(BusinessStatus.OPEN, LocalDate.of(2026, 11, 2));
		storeRepository.save(momo);

		owner(get("/api/v1/owner/stores/{id}", momo.getId()))
				.andExpect(jsonPath("$.data.businessStatus").value("CLOSED"))
				.andExpect(jsonPath("$.data.businessStatusOverride").doesNotExist())
				.andExpect(jsonPath("$.data.businessHours.openTime").doesNotExist())
				.andExpect(jsonPath("$.data.imageUrls", hasSize(0)));
	}

	@Test
	void 메뉴_목록은_대표_메뉴가_먼저다() throws Exception {
		owner(get("/api/v1/owner/stores/{id}/menus", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].name").value(contains("연어 아보카도 덮밥", "된장찌개")))
				.andExpect(jsonPath("$.data[0].signature").value(true))
				.andExpect(jsonPath("$.data[0].soldOut").value(false));
	}

	@Test
	void 할인_공지_목록은_비공개까지_최근_게시_순으로_모두_보여준다() throws Exception {
		owner(get("/api/v1/owner/stores/{id}/benefits", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].title").value("지난 공지"))
				.andExpect(jsonPath("$.data[0].published").value(false))
				.andExpect(jsonPath("$.data[1].title").value("평일 점심 10% 할인"))
				.andExpect(jsonPath("$.data[1].content").value("평일 점심 식사 메뉴 주문 시 10% 할인해 드려요."))
				.andExpect(jsonPath("$.data[1].periodText").value("11월 한 달"))
				.andExpect(jsonPath("$.data[1].published").value(true))
				.andExpect(jsonPath("$.data[1].partner").value(false))
				.andExpect(jsonPath("$.data[1].publishedAt").value("2026-11-01T09:30:00+09:00"));
	}

	@Test
	void 남의_가게는_NOT_STORE_OWNER다() throws Exception {
		for (String path : new String[] {"", "/menus", "/benefits"}) {
			owner(get("/api/v1/owner/stores/{id}" + path, others.getId()))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));
		}
		mockMvc.perform(get("/api/v1/owner/stores/{id}", onggi.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + otherOwnerToken))
				.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));
	}

	@Test
	void 없는_가게는_STORE_NOT_FOUND다() throws Exception {
		owner(get("/api/v1/owner/stores/{id}", 999_999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
	}

	@Test
	void 손님_계정은_FORBIDDEN이고_토큰이_없으면_UNAUTHORIZED다() throws Exception {
		mockMvc.perform(get("/api/v1/owner/stores").header(HttpHeaders.AUTHORIZATION, "Bearer " + consumerToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
		mockMvc.perform(get("/api/v1/owner/stores"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	private ResultActions owner(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
			throws Exception {
		return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken));
	}

	private Member saveMember(String providerId, MemberRole role) {
		return memberRepository.save(Member.builder()
				.provider(AuthProvider.KAKAO).providerId(providerId).nickname(providerId).role(role).build());
	}

	private String tokenOf(Member member) {
		return jwtProvider.issueAccessToken(new AuthMember(member.getId(), member.getRole()));
	}

	private static org.locationtech.jts.geom.Point point(double lat, double lng) {
		return GEOMETRY.createPoint(new Coordinate(lng, lat));
	}

}
