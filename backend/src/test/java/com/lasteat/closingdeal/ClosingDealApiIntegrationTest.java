package com.lasteat.closingdeal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.entity.StoreImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// 손님 마감할인 조회·예약·취소. 지금은 2026-11-03 20:30.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, ClosingDealTestSupport.FixedClockConfig.class})
class ClosingDealApiIntegrationTest extends ClosingDealTestSupport {

	@Autowired
	private MockMvc mockMvc;

	private Store onggi;

	private ClosingDeal bowl;

	private ClosingDeal soldOut;

	private ClosingDeal scheduled;

	private String token;

	private String otherToken;

	@BeforeEach
	void setUp() {
		cleanUp();
		Member member = saveMember("member", MemberRole.CONSUMER);
		Member other = saveMember("other", MemberRole.CONSUMER);
		token = tokenOf(member);
		otherToken = tokenOf(other);

		// 북쪽 약 100m
		onggi = saveStore(null, "연남 온기", StoreCategory.KOREAN, LAT + 0.0009, LNG);
		// 동쪽 약 300m
		Store momo = saveStore(null, "모모", StoreCategory.CAFE, LAT, LNG + 0.0034);
		// 북쪽 약 2km
		Store far = saveStore(null, "멀리 식당", StoreCategory.ETC, LAT + 0.018, LNG);
		storeImageRepository.save(StoreImage.builder().store(onggi).imageUrl("https://img/onggi.jpg").sortOrder(0)
				.build());

		Menu bowlMenu = saveMenu(onggi, "연어 덮밥", 15000);
		bowl = saveDeal(bowlMenu, 9000, 3, NOW.minusMinutes(30), NOW.plusHours(1));
		scheduled = saveDeal(saveMenu(onggi, "된장찌개", 9000), 5000, 3, NOW.plusMinutes(30), NOW.plusHours(2));
		soldOut = saveDeal(saveMenu(momo, "샌드위치", 6000), 3000, 0, NOW.minusHours(1), NOW.plusMinutes(30));
		ClosingDeal closed = saveDeal(saveMenu(momo, "케이크", 7000), 3500, 2, NOW.minusHours(1), NOW.plusHours(1));
		closed.close();
		closingDealRepository.save(closed);
		saveDeal(saveMenu(momo, "지난 빵", 4000), 2000, 2, NOW.minusHours(3), NOW.minusMinutes(1));
		saveDeal(saveMenu(far, "멀리 국밥", 10000), 5000, 2, NOW.minusMinutes(10), NOW.plusHours(1));
	}

	@Test
	void 주변_마감할인은_노출_중인_것만_가까운_순이다() throws Exception {
		search("")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[*].menu.name").value(contains("연어 덮밥", "샌드위치")))
				.andExpect(jsonPath("$.data.totalElements").value(2))
				.andExpect(jsonPath("$.data.content[0].status").value("OPEN"))
				.andExpect(jsonPath("$.data.content[0].originalPrice").value(15000))
				.andExpect(jsonPath("$.data.content[0].salePrice").value(9000))
				.andExpect(jsonPath("$.data.content[0].discountRate").value(40))
				.andExpect(jsonPath("$.data.content[0].stock").value(3))
				.andExpect(jsonPath("$.data.content[0].openAt").value("2026-11-03T20:00:00+09:00"))
				.andExpect(jsonPath("$.data.content[0].pickupEndAt").value("2026-11-03T21:30:00+09:00"))
				.andExpect(jsonPath("$.data.content[0].store.name").value("연남 온기"))
				.andExpect(jsonPath("$.data.content[0].store.thumbnailUrl").value("https://img/onggi.jpg"))
				.andExpect(jsonPath("$.data.content[0].store.distance")
						.value(allOf(greaterThanOrEqualTo(95), lessThanOrEqualTo(105))))
				.andExpect(jsonPath("$.data.content[1].status").value("SOLD_OUT"));
	}

	@Test
	void 품절_제외_카테고리_마감_임박_순_반경을_지원한다() throws Exception {
		search("&available=true").andExpect(jsonPath("$.data.content[*].menu.name").value(contains("연어 덮밥")));
		search("&category=CAFE").andExpect(jsonPath("$.data.content[*].menu.name").value(contains("샌드위치")));
		search("&sort=ENDING_SOON")
				.andExpect(jsonPath("$.data.content[*].menu.name").value(contains("샌드위치", "연어 덮밥")));
		search("&radius=3000").andExpect(jsonPath("$.data.totalElements").value(3));
		search("&sort=CHEAPEST").andExpect(status().isBadRequest());
	}

	@Test
	void 가게_마감할인과_상세() throws Exception {
		mockMvc.perform(get("/api/v1/stores/{id}/closing-deals", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].menu.name").value(contains("연어 덮밥")))
				.andExpect(jsonPath("$.data[0].store.distance").doesNotExist());

		mockMvc.perform(get("/api/v1/closing-deals/{id}", bowl.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.menu.description").value("연어 덮밥 설명"))
				.andExpect(jsonPath("$.data.store.address").value("서울 동작구 상도로 연남 온기"))
				.andExpect(jsonPath("$.data.store.phone").value("02-000-0000"))
				.andExpect(jsonPath("$.data.myReservationId").doesNotExist());
		mockMvc.perform(get("/api/v1/closing-deals/{id}", scheduled.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("CLOSING_DEAL_NOT_FOUND"));
	}

	@Test
	void 예약하면_재고가_줄고_예약_상세와_내_목록에_나온다() throws Exception {
		Long reservationId = reserve(bowl.getId(), 2, token)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("RESERVED"))
				.andExpect(jsonPath("$.data.quantity").value(2))
				.andExpect(jsonPath("$.data.totalPrice").value(18000))
				.andExpect(jsonPath("$.data.reservedAt").value("2026-11-03T20:30:00+09:00"))
				.andExpect(jsonPath("$.data.pickedUpAt").doesNotExist())
				.andExpect(jsonPath("$.data.pickupEndAt").value("2026-11-03T21:30:00+09:00"))
				.andExpect(jsonPath("$.data.closingDeal.menuName").value("연어 덮밥"))
				.andExpect(jsonPath("$.data.closingDeal.salePrice").value(9000))
				.andExpect(jsonPath("$.data.store.name").value("연남 온기"))
				.andReturn().getResponse().getContentAsString().transform(ClosingDealApiIntegrationTest::idOf);

		assertThat(closingDealRepository.findById(bowl.getId()).orElseThrow().getStock()).isEqualTo(1);
		as(token, get("/api/v1/closing-deals/{id}", bowl.getId()))
				.andExpect(jsonPath("$.data.stock").value(1))
				.andExpect(jsonPath("$.data.myReservationId").value(reservationId));
		as(token, get("/api/v1/reservations/{id}", reservationId))
				.andExpect(jsonPath("$.data.id").value(reservationId));
		as(token, get("/api/v1/members/me/reservations?status=RESERVED"))
				.andExpect(jsonPath("$.data.content[*].id").value(contains(reservationId.intValue())));
		as(token, get("/api/v1/members/me/reservations?status=PICKED_UP,CANCELED,NO_SHOW"))
				.andExpect(jsonPath("$.data.totalElements").value(0));
		as(token, get("/api/v1/members/me/reservations?page=2147483647&size=100"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("page"));
		as(otherToken, get("/api/v1/reservations/{id}", reservationId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("RESERVATION_NOT_FOUND"));
	}

	@Test
	void 예약할_수_없는_경우의_에러() throws Exception {
		mockMvc.perform(post("/api/v1/closing-deals/{id}/reservations", bowl.getId())
						.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\": 1}"))
				.andExpect(status().isUnauthorized());
		reserve(bowl.getId(), 6, token).andExpect(jsonPath("$.error.fields[0].field").value("quantity"));
		reserve(bowl.getId(), 0, token).andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		reserve(999_999L, 1, token).andExpect(jsonPath("$.error.code").value("CLOSING_DEAL_NOT_FOUND"));
		reserve(scheduled.getId(), 1, token)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("CLOSING_DEAL_NOT_OPEN"));
		reserve(soldOut.getId(), 1, token).andExpect(jsonPath("$.error.code").value("OUT_OF_STOCK"));
		reserve(bowl.getId(), 4, token).andExpect(jsonPath("$.error.code").value("OUT_OF_STOCK"));

		reserve(bowl.getId(), 1, token).andExpect(status().isOk());
		reserve(bowl.getId(), 1, token).andExpect(jsonPath("$.error.code").value("ALREADY_RESERVED"));
		assertThat(closingDealRepository.findById(bowl.getId()).orElseThrow().getStock()).isEqualTo(2);
	}

	@Test
	void 취소하면_재고가_돌아오고_다시_취소할_수_없다() throws Exception {
		Long reservationId = reserve(bowl.getId(), 3, token).andReturn().getResponse().getContentAsString()
				.transform(ClosingDealApiIntegrationTest::idOf);
		as(token, get("/api/v1/closing-deals/{id}", bowl.getId())).andExpect(jsonPath("$.data.status")
				.value("SOLD_OUT"));

		as(otherToken, post("/api/v1/reservations/{id}/cancel", reservationId))
				.andExpect(jsonPath("$.error.code").value("RESERVATION_NOT_FOUND"));
		as(token, post("/api/v1/reservations/{id}/cancel", reservationId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("CANCELED"));
		as(token, post("/api/v1/reservations/{id}/cancel", reservationId))
				.andExpect(jsonPath("$.error.code").value("RESERVATION_NOT_CANCELABLE"));

		ClosingDeal reloaded = closingDealRepository.findById(bowl.getId()).orElseThrow();
		assertThat(reloaded.getStock()).isEqualTo(3);
		// 취소했으니 다시 예약할 수 있다
		reserve(bowl.getId(), 1, token).andExpect(status().isOk());
	}

	@Test
	void 수령_마감이_지난_예약은_취소할_수_없다() throws Exception {
		Member member = memberRepository.findAll().stream().filter(m -> m.getProviderId().equals("member"))
				.findFirst().orElseThrow();
		ClosingDeal expired = saveDeal(saveMenu(onggi, "지난 메뉴", 8000), 4000, 1, NOW.minusHours(2),
				NOW.minusMinutes(5));
		Reservation reservation = reservationRepository.save(Reservation.builder().member(member)
				.closingDeal(expired).quantity(1).build());

		as(token, post("/api/v1/reservations/{id}/cancel", reservation.getId()))
				.andExpect(jsonPath("$.error.code").value("RESERVATION_NOT_CANCELABLE"));
		assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus())
				.isEqualTo(ReservationStatus.RESERVED);
	}

	private ResultActions search(String query) throws Exception {
		return mockMvc.perform(get("/api/v1/closing-deals?lat=" + LAT + "&lng=" + LNG + query));
	}

	private ResultActions reserve(Long dealId, int quantity, String accessToken) throws Exception {
		return as(accessToken, post("/api/v1/closing-deals/{id}/reservations", dealId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\": " + quantity + "}"));
	}

	private ResultActions as(String accessToken, MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
	}

	// 응답 JSON의 data.id
	private static Long idOf(String json) {
		var matcher = java.util.regex.Pattern.compile("\"data\":\\{\"id\":(\\d+)").matcher(json);
		if (!matcher.find()) {
			throw new IllegalStateException("id가 없습니다: " + json);
		}
		return Long.valueOf(matcher.group(1));
	}

}
