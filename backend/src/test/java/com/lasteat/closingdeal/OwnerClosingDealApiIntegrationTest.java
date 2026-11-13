package com.lasteat.closingdeal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.ClosingDealStatus;
import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
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

// 사장님 마감할인 등록·재고 수정·조기 마감, 예약 목록·수령·노쇼. 지금은 2026-11-03 20:30.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, ClosingDealTestSupport.FixedClockConfig.class})
class OwnerClosingDealApiIntegrationTest extends ClosingDealTestSupport {

	private static final String BASE = "/api/v1/owner/stores/{storeId}";

	@Autowired
	private MockMvc mockMvc;

	private Store onggi;

	private Store others;

	private Menu bowlMenu;

	private Menu othersMenu;

	private ClosingDeal bowl;

	private Member customer;

	private String ownerToken;

	@BeforeEach
	void setUp() {
		cleanUp();
		Member owner = saveMember("owner", MemberRole.OWNER);
		Member otherOwner = saveMember("other-owner", MemberRole.OWNER);
		customer = saveMember("customer", MemberRole.CONSUMER);
		ownerToken = tokenOf(owner);

		onggi = saveStore(owner, "연남 온기", StoreCategory.KOREAN, LAT, LNG);
		others = saveStore(otherOwner, "남의 가게", StoreCategory.ETC, LAT, LNG);
		bowlMenu = saveMenu(onggi, "연어 덮밥", 15000);
		othersMenu = saveMenu(others, "남의 메뉴", 8000);
		bowl = saveDeal(bowlMenu, 9000, 5, NOW.minusMinutes(30), NOW.plusHours(1));
	}

	@Test
	void 등록하면_정가는_메뉴_가격이고_노출_시작이_지났으면_바로_예약_가능이다() throws Exception {
		owner(post(BASE + "/closing-deals", onggi.getId()), dealBody(bowlMenu.getId(), 7000, 4,
				"2026-11-03T20:00:00+09:00", "2026-11-03T22:00:00+09:00"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("OPEN"))
				.andExpect(jsonPath("$.data.menu.name").value("연어 덮밥"))
				.andExpect(jsonPath("$.data.originalPrice").value(15000))
				.andExpect(jsonPath("$.data.salePrice").value(7000))
				.andExpect(jsonPath("$.data.discountRate").value(53))
				.andExpect(jsonPath("$.data.stock").value(4))
				.andExpect(jsonPath("$.data.reservedQuantity").value(0))
				.andExpect(jsonPath("$.data.pickedUpQuantity").value(0))
				.andExpect(jsonPath("$.data.openAt").value("2026-11-03T20:00:00+09:00"));

		owner(post(BASE + "/closing-deals", onggi.getId()), dealBody(bowlMenu.getId(), 7000, 4,
				"2026-11-03T21:00:00+09:00", "2026-11-03T22:00:00+09:00"))
				.andExpect(jsonPath("$.data.status").value("SCHEDULED"));
	}

	@Test
	void 등록_값이_맞지_않으면_어느_칸이_틀렸는지_알려_준다() throws Exception {
		String path = BASE + "/closing-deals";
		owner(post(path, onggi.getId()), dealBody(bowlMenu.getId(), 15000, 4,
				"2026-11-03T20:00:00+09:00", "2026-11-03T22:00:00+09:00"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.fields[0].field").value("salePrice"));
		owner(post(path, onggi.getId()), dealBody(bowlMenu.getId(), 7000, 4,
				"2026-11-03T22:00:00+09:00", "2026-11-03T21:00:00+09:00"))
				.andExpect(jsonPath("$.error.fields[0].field").value("pickupEndAt"));
		owner(post(path, onggi.getId()), dealBody(bowlMenu.getId(), 7000, 4,
				"2026-11-03T18:00:00+09:00", "2026-11-03T20:00:00+09:00"))
				.andExpect(jsonPath("$.error.fields[0].field").value("pickupEndAt"));
		owner(post(path, onggi.getId()), dealBody(bowlMenu.getId(), 7000, 4,
				"2026-11-03T21:00:00+09:00", "2026-11-04T22:00:00+09:00"))
				.andExpect(jsonPath("$.error.fields[0].field").value("pickupEndAt"));
		owner(post(path, onggi.getId()), dealBody(bowlMenu.getId(), 7000, 100,
				"2026-11-03T21:00:00+09:00", "2026-11-03T22:00:00+09:00"))
				.andExpect(jsonPath("$.error.fields[0].field").value("stock"));
		owner(post(path, onggi.getId()), dealBody(othersMenu.getId(), 4000, 1,
				"2026-11-03T21:00:00+09:00", "2026-11-03T22:00:00+09:00"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MENU_NOT_FOUND"));
		owner(post(path, others.getId()), dealBody(othersMenu.getId(), 4000, 1,
				"2026-11-03T21:00:00+09:00", "2026-11-03T22:00:00+09:00"))
				.andExpect(jsonPath("$.error.code").value("NOT_STORE_OWNER"));

		assertThat(closingDealRepository.count()).isEqualTo(1);
	}

	@Test
	void 목록은_오늘_노출_시작한_것을_예약_수령_수량과_함께_보여준다() throws Exception {
		saveDeal(bowlMenu, 8000, 2, NOW.minusDays(1), NOW.minusDays(1).plusHours(1));
		reservationRepository.save(reservation(bowl, 2));
		Reservation pickedUp = reservation(bowl, 1);
		pickedUp.pickUp(NOW);
		reservationRepository.save(pickedUp);
		Reservation canceled = reservation(bowl, 1);
		canceled.cancel();
		reservationRepository.save(canceled);

		owner(get(BASE + "/closing-deals", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].id").value(bowl.getId()))
				.andExpect(jsonPath("$.data[0].reservedQuantity").value(2))
				.andExpect(jsonPath("$.data[0].pickedUpQuantity").value(1));
		owner(get(BASE + "/closing-deals?date=2026-11-02", onggi.getId()))
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].status").value("CLOSED"));
	}

	@Test
	void 재고를_수정하고_조기_마감하면_더_이상_바꿀_수_없다() throws Exception {
		owner(patch(BASE + "/closing-deals/{dealId}/stock", onggi.getId(), bowl.getId()), "{\"stock\": 0}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.stock").value(0))
				.andExpect(jsonPath("$.data.status").value("SOLD_OUT"));
		owner(patch(BASE + "/closing-deals/{dealId}/stock", onggi.getId(), bowl.getId()), "{\"stock\": -1}")
				.andExpect(jsonPath("$.error.fields[0].field").value("stock"));

		owner(post(BASE + "/closing-deals/{dealId}/close", onggi.getId(), bowl.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("CLOSED"));
		owner(patch(BASE + "/closing-deals/{dealId}/stock", onggi.getId(), bowl.getId()), "{\"stock\": 3}")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("CLOSING_DEAL_NOT_OPEN"));
		owner(post(BASE + "/closing-deals/{dealId}/close", onggi.getId(), bowl.getId()))
				.andExpect(jsonPath("$.error.code").value("CLOSING_DEAL_NOT_OPEN"));

		ClosingDeal othersDeal = saveDeal(othersMenu, 4000, 1, NOW.minusMinutes(5), NOW.plusHours(1));
		owner(post(BASE + "/closing-deals/{dealId}/close", onggi.getId(), othersDeal.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("CLOSING_DEAL_NOT_FOUND"));
	}

	@Test
	void 예약_목록은_수령_대기를_먼저_보여주고_손님_닉네임과_결제_금액이_나온다() throws Exception {
		Reservation done = reservation(bowl, 1);
		done.pickUp(NOW);
		reservationRepository.save(done);
		Reservation waiting = reservationRepository.save(reservation(bowl, 2));

		owner(get(BASE + "/reservations", onggi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[*].id").value(contains(waiting.getId().intValue(),
						done.getId().intValue())))
				.andExpect(jsonPath("$.data.content[0].memberNickname").value("customer"))
				.andExpect(jsonPath("$.data.content[0].totalPrice").value(18000))
				.andExpect(jsonPath("$.data.content[0].closingDeal.menuName").value("연어 덮밥"))
				.andExpect(jsonPath("$.data.content[0].closingDeal.pickupEndAt").value("2026-11-03T21:30:00+09:00"));
		owner(get(BASE + "/reservations?status=PICKED_UP", onggi.getId()))
				.andExpect(jsonPath("$.data.content[*].id").value(contains(done.getId().intValue())));
		owner(get(BASE + "/reservations?closingDealId=999999", onggi.getId()))
				.andExpect(jsonPath("$.data.totalElements").value(0));
	}

	@Test
	void 수령_완료와_노쇼_처리() throws Exception {
		bowl.decreaseStock(3);
		closingDealRepository.save(bowl);
		Reservation first = reservationRepository.save(reservation(bowl, 1));
		Reservation second = reservationRepository.save(reservation(bowl, 2));
		String action = BASE + "/reservations/{reservationId}/";

		owner(post(action + "pickup", onggi.getId(), first.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("PICKED_UP"))
				.andExpect(jsonPath("$.data.pickedUpAt").value("2026-11-03T20:30:00+09:00"));
		owner(post(action + "no-show", onggi.getId(), first.getId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("INVALID_RESERVATION_STATUS"));

		owner(post(action + "no-show", onggi.getId(), second.getId()))
				.andExpect(jsonPath("$.data.status").value("NO_SHOW"));
		// 노쇼한 2개는 다시 예약할 수 있게 재고로 돌아온다
		assertThat(closingDealRepository.findById(bowl.getId()).orElseThrow().getStock()).isEqualTo(4);

		Reservation othersReservation = reservationRepository.save(reservation(
				saveDeal(othersMenu, 4000, 1, NOW.minusMinutes(5), NOW.plusHours(1)), 1));
		owner(post(action + "pickup", onggi.getId(), othersReservation.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("RESERVATION_NOT_FOUND"));
		assertThat(reservationRepository.findById(othersReservation.getId()).orElseThrow().getStatus())
				.isEqualTo(ReservationStatus.RESERVED);
	}

	@Test
	void 손님_계정은_사장님_API를_쓸_수_없다() throws Exception {
		mockMvc.perform(get(BASE + "/closing-deals", onggi.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOf(customer)))
				.andExpect(status().isForbidden());
		assertThat(closingDealRepository.findById(bowl.getId()).orElseThrow().getStatus())
				.isEqualTo(ClosingDealStatus.SCHEDULED);
	}

	private Reservation reservation(ClosingDeal deal, int quantity) {
		return Reservation.builder().member(customer).closingDeal(deal).quantity(quantity).build();
	}

	private ResultActions owner(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken));
	}

	private ResultActions owner(MockHttpServletRequestBuilder request, String body) throws Exception {
		return owner(request.contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private static String dealBody(Long menuId, int salePrice, int stock, String openAt, String pickupEndAt) {
		return """
				{"menuId": %d, "salePrice": %d, "stock": %d, "openAt": "%s", "pickupEndAt": "%s"}
				""".formatted(menuId, salePrice, stock, openAt, pickupEndAt);
	}

}
