package com.lasteat.benefit;

import static com.lasteat.benefit.entity.BenefitFixture.PUBLISHED_AT;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.benefit.entity.Benefit;
import com.lasteat.benefit.entity.BenefitFixture;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.entity.StoreImage;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// 손님용 할인 공지 조회 (공개 API). 실제 MySQL 공간 함수로 반경을 확인한다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class BenefitApiIntegrationTest {

	private static final double LAT = 37.4963;

	private static final double LNG = 126.9575;

	private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

	@Autowired
	private MockMvc mockMvc;

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

	private Benefit lunch;

	private Benefit hidden;

	@BeforeEach
	void setUp() {
		bookmarkRepository.deleteAllInBatch();
		benefitRepository.deleteAllInBatch();
		menuRepository.deleteAllInBatch();
		storeImageRepository.deleteAllInBatch();
		storeRepository.deleteAllInBatch();
		refreshTokenRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();

		// 북쪽 약 100m
		onggi = storeRepository.save(newStore("연남 온기", StoreCategory.KOREAN, LAT + 0.0009, LNG));
		// 동쪽 약 300m
		Store momo = storeRepository.save(newStore("모모", StoreCategory.CAFE, LAT, LNG + 0.0034));
		// 북쪽 약 2km
		Store far = storeRepository.save(newStore("멀리 식당", StoreCategory.ETC, LAT + 0.018, LNG));
		storeImageRepository.save(StoreImage.builder().store(onggi).imageUrl("https://img/onggi.jpg").sortOrder(0)
				.build());

		lunch = benefitRepository.save(BenefitFixture.notice(onggi, "평일 점심 10% 할인"));
		benefitRepository.save(BenefitFixture.notice(onggi, "저녁 음료 무료", PUBLISHED_AT.plusDays(1)));
		Benefit partner = BenefitFixture.notice(momo, "숭실대 학생 제휴 20%");
		partner.changePartner(true);
		benefitRepository.save(partner);
		hidden = BenefitFixture.notice(momo, "지난 공지", PUBLISHED_AT.plusDays(2));
		hidden.changePublished(false);
		benefitRepository.save(hidden);
		benefitRepository.save(BenefitFixture.notice(far, "멀리 할인"));
	}

	@Test
	void 주변_공지는_가까운_순이고_같은_가게는_최근_게시_순이며_비공개는_빠진다() throws Exception {
		search("")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[*].title")
						.value(contains("저녁 음료 무료", "평일 점심 10% 할인", "숭실대 학생 제휴 20%")))
				.andExpect(jsonPath("$.data.totalElements").value(3))
				.andExpect(jsonPath("$.data.content[1].content").value("평일 점심 식사 메뉴 주문 시 10% 할인해 드려요."))
				.andExpect(jsonPath("$.data.content[1].periodText").value("11월 한 달"))
				.andExpect(jsonPath("$.data.content[1].partner").value(false))
				.andExpect(jsonPath("$.data.content[1].publishedAt").value("2026-11-01T09:30:00+09:00"))
				.andExpect(jsonPath("$.data.content[1].store.name").value("연남 온기"))
				.andExpect(jsonPath("$.data.content[1].store.category").value("KOREAN"))
				.andExpect(jsonPath("$.data.content[1].store.thumbnailUrl").value("https://img/onggi.jpg"))
				.andExpect(jsonPath("$.data.content[1].store.distance")
						.value(allOf(greaterThanOrEqualTo(95), lessThanOrEqualTo(105))))
				.andExpect(jsonPath("$.data.content[2].store.thumbnailUrl").doesNotExist());
	}

	@Test
	void 반경을_넓히면_먼_가게_공지도_나온다() throws Exception {
		search("&radius=3000")
				.andExpect(jsonPath("$.data.totalElements").value(4))
				.andExpect(jsonPath("$.data.content[3].title").value("멀리 할인"));
	}

	@Test
	void 제휴_혜택만_거른다() throws Exception {
		search("&partner=true")
				.andExpect(jsonPath("$.data.content[*].title").value(contains("숭실대 학생 제휴 20%")))
				.andExpect(jsonPath("$.data.content[0].partner").value(true));
	}

	@Test
	void 페이지로_나눠_보여준다() throws Exception {
		search("&size=2")
				.andExpect(jsonPath("$.data.content", hasSize(2)))
				.andExpect(jsonPath("$.data.hasNext").value(true));
		search("&size=2&page=1")
				.andExpect(jsonPath("$.data.content[*].title").value(contains("숭실대 학생 제휴 20%")))
				.andExpect(jsonPath("$.data.hasNext").value(false));
	}

	@Test
	void 좌표가_없거나_반경이_크면_입력값_오류다() throws Exception {
		mockMvc.perform(get("/api/v1/benefits").param("lng", String.valueOf(LNG)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("lat"));
		search("&radius=3001").andExpect(jsonPath("$.error.fields[0].field").value("radius"));
	}

	@Test
	void 공지_상세는_공개_중일_때만_보인다() throws Exception {
		mockMvc.perform(get("/api/v1/benefits/{id}", lunch.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("평일 점심 10% 할인"))
				.andExpect(jsonPath("$.data.store.id").value(onggi.getId()))
				.andExpect(jsonPath("$.data.store.distance").doesNotExist());
		mockMvc.perform(get("/api/v1/benefits/{id}", hidden.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("BENEFIT_NOT_FOUND"));
		mockMvc.perform(get("/api/v1/benefits/{id}", 999_999))
				.andExpect(jsonPath("$.error.code").value("BENEFIT_NOT_FOUND"));
	}

	@Test
	void 가게_공지_목록은_공개_중인_것만_최근_게시_순이다() throws Exception {
		mockMvc.perform(get("/api/v1/stores/{id}/benefits", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].title").value(contains("저녁 음료 무료", "평일 점심 10% 할인")))
				.andExpect(jsonPath("$.data[0].store.name").value("연남 온기"));
		mockMvc.perform(get("/api/v1/stores/{id}/benefits", 999_999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
	}

	private ResultActions search(String query) throws Exception {
		return mockMvc.perform(get("/api/v1/benefits?lat=" + LAT + "&lng=" + LNG + query));
	}

	private static Store newStore(String name, StoreCategory category, double lat, double lng) {
		return Store.builder()
				.name(name).category(category).address("서울 동작구 상도로")
				.location(GEOMETRY.createPoint(new Coordinate(lng, lat)))
				.build();
	}

}
