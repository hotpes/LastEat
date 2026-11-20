package com.lasteat.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.closingdeal.repository.ClosingDealRepository;
import com.lasteat.closingdeal.repository.ReservationRepository;
import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.entity.StoreImage;
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

// 사장님 가게 정보·영업시간·오늘 영업 상태 변경과 손님 조회 반영. 시각은 2026-11-03(화) 12:00으로 고정한다.
@SpringBootTest(properties = "lasteat.scheduler.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OwnerStoreWriteApiIntegrationTest.FixedClockConfig.class})
class OwnerStoreWriteApiIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 0);

	private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

	private static final String VALID_INFO = """
			{"name": " 상도 온기 ", "category": "CAFE", "address": " 서울 동작구 상도로 55 ",
			 "lat": 37.5001, "lng": 126.9501, "description": " 새 소개 ", "phone": "02-123-4567",
			 "imageUrls": ["https://cdn.lasteat.app/b.jpg", "https://cdn.lasteat.app/c.jpg"],
			 "tags": [" 포장 가능 ", "혼밥"]}
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

	private Store others;

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
		others = storeRepository.save(Store.builder()
				.owner(otherOwner).name("남의 가게").category(StoreCategory.ETC).address("서울 동작구 상도로 9")
				.location(point(37.4963, 126.9575))
				.build());
		storeImageRepository.save(StoreImage.builder().store(onggi).imageUrl("https://cdn.lasteat.app/a.jpg")
				.sortOrder(0).build());
	}

	@AfterEach
	void tearDown() {
		cleanUp();
	}

	@Test
	void 가게_정보를_수정하면_공백을_정리해_저장하고_손님_상세에_반영된다() throws Exception {
		owner(put("/api/v1/owner/stores/{id}", onggi.getId()), VALID_INFO)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("상도 온기"))
				.andExpect(jsonPath("$.data.category").value("CAFE"))
				.andExpect(jsonPath("$.data.address").value("서울 동작구 상도로 55"))
				.andExpect(jsonPath("$.data.description").value("새 소개"))
				.andExpect(jsonPath("$.data.tags").value(contains("포장 가능", "혼밥")))
				.andExpect(jsonPath("$.data.imageUrls")
						.value(contains("https://cdn.lasteat.app/b.jpg", "https://cdn.lasteat.app/c.jpg")))
				// 영업시간은 그대로
				.andExpect(jsonPath("$.data.businessHours.openTime").value("11:00"));

		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()))
				.andExpect(jsonPath("$.data.name").value("상도 온기"))
				.andExpect(jsonPath("$.data.phone").value("02-123-4567"))
				.andExpect(jsonPath("$.data.lat").value(closeTo(37.5001, 1e-9)))
				.andExpect(jsonPath("$.data.lng").value(closeTo(126.9501, 1e-9)));
		// 새 좌표 주변 반경 검색에 잡힌다 (좌표 축 순서 확인)
		mockMvc.perform(get("/api/v1/stores").param("lat", "37.5001").param("lng", "126.9501")
						.param("radius", "100"))
				.andExpect(jsonPath("$.data.content[*].name").value(contains("상도 온기")));
	}

	@Test
	void 사진과_태그를_생략하면_비우고_빈_설명과_전화번호는_null이다() throws Exception {
		owner(put("/api/v1/owner/stores/{id}", onggi.getId()), """
				{"name": "연남 온기", "category": "KOREAN", "address": "서울 동작구 상도로 1",
				 "lat": 37.4972, "lng": 126.9575, "description": "  ", "phone": ""}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.imageUrls", hasSize(0)))
				.andExpect(jsonPath("$.data.tags", hasSize(0)))
				.andExpect(jsonPath("$.data.description").doesNotExist())
				.andExpect(jsonPath("$.data.phone").doesNotExist());
		assertThat(storeImageRepository.findAllByStoreId(onggi.getId())).isEmpty();
	}

	@Test
	void 가게_정보_입력값이_잘못되면_INVALID_INPUT이고_바뀌지_않는다() throws Exception {
		String base = """
				"category": "KOREAN", "address": "서울", "lat": 37.4972, "lng": 126.9575""";
		String[] bodies = {
				"{\"name\": \"   \", " + base + "}",
				"{\"name\": \"　\", " + base + "}",
				"{\"name\": \"" + "가".repeat(31) + "\", " + base + "}",
				"{\"name\": \"온기\", \"category\": \"PIZZA\", \"address\": \"서울\", \"lat\": 37, \"lng\": 126}",
				"{\"name\": \"온기\", \"category\": \"KOREAN\", \"address\": \"서울\", \"lat\": 91, \"lng\": 126}",
				"{\"name\": \"온기\", \"category\": \"KOREAN\", \"address\": \"서울\", \"lng\": 126}",
				"{\"name\": \"온기\", " + base + ", \"phone\": \"02)123\"}",
				"{\"name\": \"온기\", " + base + ", \"tags\": [\"a\", \"b\", \"c\", \"d\", \"e\", \"f\"]}",
				"{\"name\": \"온기\", " + base + ", \"tags\": [\"" + "가".repeat(11) + "\"]}",
				"{\"name\": \"온기\", " + base + ", \"tags\": [\"포장,배달\"]}",
				"{\"name\": \"온기\", " + base + ", \"imageUrls\": [\" \"]}",
		};
		for (String body : bodies) {
			owner(put("/api/v1/owner/stores/{id}", onggi.getId()), body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		}
		assertThat(storeRepository.findById(onggi.getId())).get().extracting(Store::getName).isEqualTo("연남 온기");
		assertThat(storeImageRepository.findAllByStoreId(onggi.getId())).hasSize(1);
	}

	@Test
	void 남의_가게는_수정할_수_없다() throws Exception {
		owner(put("/api/v1/owner/stores/{id}", others.getId()), VALID_INFO)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));
		owner(put("/api/v1/owner/stores/{id}/hours", others.getId()), "{\"openTime\": \"09:00\", \"closeTime\": \"18:00\"}")
				.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));
		owner(patch("/api/v1/owner/stores/{id}/business-status", others.getId()), "{\"status\": \"CLOSED\"}")
				.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));
		owner(put("/api/v1/owner/stores/{id}", 999_999), VALID_INFO)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
		assertThat(storeRepository.findById(others.getId())).get().extracting(Store::getName).isEqualTo("남의 가게");
	}

	@Test
	void 영업시간을_바꾸면_손님_상세와_영업_상태에_반영된다() throws Exception {
		// 12:00에 15:00~17:00 쉬는 시간이 아니고, 화요일이 휴무면 CLOSED
		owner(put("/api/v1/owner/stores/{id}/hours", onggi.getId()), """
				{"openTime": "10:00", "closeTime": "02:00", "breakStart": "15:00", "breakEnd": "17:00",
				 "closedDays": ["TUE", "SUN"]}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.businessHours.openTime").value("10:00"))
				.andExpect(jsonPath("$.data.businessHours.closeTime").value("02:00"))
				.andExpect(jsonPath("$.data.businessHours.breakStart").value("15:00"))
				.andExpect(jsonPath("$.data.businessHours.closedDays").value(containsInAnyOrder("TUE", "SUN")))
				.andExpect(jsonPath("$.data.businessStatus").value("CLOSED"));

		owner(put("/api/v1/owner/stores/{id}/hours", onggi.getId()), """
				{"openTime": "11:30", "closeTime": "20:00"}
				""")
				.andExpect(jsonPath("$.data.businessHours.breakStart").doesNotExist())
				.andExpect(jsonPath("$.data.businessHours.closedDays", hasSize(0)))
				.andExpect(jsonPath("$.data.businessStatus").value("OPEN"));
		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()))
				.andExpect(jsonPath("$.data.businessHours.openTime").value("11:30"))
				.andExpect(jsonPath("$.data.businessStatus").value("OPEN"));
	}

	@Test
	void 영업시간_입력값이_잘못되면_INVALID_INPUT이다() throws Exception {
		String[] bodies = {
				"{\"closeTime\": \"21:00\"}",
				"{\"openTime\": \"25:00\", \"closeTime\": \"21:00\"}",
				"{\"openTime\": \"11:00\", \"closeTime\": \"21:00\", \"breakStart\": \"15:00\"}",
				"{\"openTime\": \"11:00\", \"closeTime\": \"21:00\", \"breakStart\": \"17:00\", \"breakEnd\": \"15:00\"}",
				"{\"openTime\": \"11:00\", \"closeTime\": \"21:00\", \"closedDays\": [\"MONDAY\"]}",
		};
		for (String body : bodies) {
			owner(put("/api/v1/owner/stores/{id}/hours", onggi.getId()), body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		}
		assertThat(storeRepository.findById(onggi.getId())).get()
				.extracting(Store::getOpenTime).isEqualTo(LocalTime.of(11, 0));
	}

	@Test
	void 오늘_영업_상태를_정하면_손님에게_그_상태로_보이고_AUTO로_되돌린다() throws Exception {
		owner(patch("/api/v1/owner/stores/{id}/business-status", onggi.getId()), "{\"status\": \"CLOSED\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.businessStatus").value("CLOSED"))
				.andExpect(jsonPath("$.data.businessStatusOverride").value("CLOSED"));
		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()))
				.andExpect(jsonPath("$.data.businessStatus").value("CLOSED"));
		owner(get("/api/v1/owner/stores/{id}", onggi.getId()), null)
				.andExpect(jsonPath("$.data.businessStatusOverride").value("CLOSED"));
		assertThat(storeRepository.findById(onggi.getId())).get()
				.extracting(Store::getBusinessStatusOverrideDate).isEqualTo(NOW.toLocalDate());

		owner(patch("/api/v1/owner/stores/{id}/business-status", onggi.getId()), "{\"status\": \"AUTO\"}")
				.andExpect(jsonPath("$.data.businessStatus").value("OPEN"))
				.andExpect(jsonPath("$.data.businessStatusOverride").doesNotExist());
		mockMvc.perform(get("/api/v1/stores/{id}", onggi.getId()))
				.andExpect(jsonPath("$.data.businessStatus").value("OPEN"));
	}

	@Test
	void 영업_상태_값이_잘못되면_INVALID_INPUT이다() throws Exception {
		for (String body : new String[] {"{}", "{\"status\": \"SLEEP\"}"}) {
			owner(patch("/api/v1/owner/stores/{id}/business-status", onggi.getId()), body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		}
	}

	@Test
	void 손님_계정은_사장님_API를_쓸_수_없다() throws Exception {
		Member customer = saveMember("customer", MemberRole.CONSUMER);
		String token = jwtProvider.issueAccessToken(new AuthMember(customer.getId(), customer.getRole()));
		mockMvc.perform(patch("/api/v1/owner/stores/{id}/business-status", onggi.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"CLOSED\"}"))
				.andExpect(status().isForbidden());
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
