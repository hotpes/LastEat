package com.lasteat.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.auth.entity.RefreshToken;
import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.entity.Bookmark;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.Reservation;
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
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
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

// 내 정보·푸시 토큰·탈퇴
@SpringBootTest(properties = "lasteat.scheduler.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class MemberApiIntegrationTest {

	private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

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

	private Member customer;

	private Store store;

	@BeforeEach
	void setUp() {
		cleanUp();
		customer = memberRepository.save(Member.builder().provider(AuthProvider.KAKAO).providerId("1001")
				.email("me@kakao.com").nickname("맛집탐험가").role(MemberRole.CONSUMER).build());
		store = storeRepository.save(Store.builder().name("연남 온기").category(StoreCategory.KOREAN)
				.address("서울 동작구 상도로 1").location(GEOMETRY.createPoint(new Coordinate(126.9575, 37.4972)))
				.build());
	}

	@AfterEach
	void tearDown() {
		cleanUp();
	}

	@Test
	void 내_정보를_조회한다() throws Exception {
		as(customer, get("/api/v1/members/me"), null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(customer.getId()))
				.andExpect(jsonPath("$.data.nickname").value("맛집탐험가"))
				.andExpect(jsonPath("$.data.email").value("me@kakao.com"))
				.andExpect(jsonPath("$.data.role").value("CONSUMER"))
				.andExpect(jsonPath("$.data.ownedStoreCount").value(0));
	}

	@Test
	void 사장님은_연결된_가게_수가_나온다() throws Exception {
		Member owner = memberRepository.save(Member.builder().provider(AuthProvider.KAKAO).providerId("2002")
				.nickname("사장님").role(MemberRole.OWNER).build());
		storeRepository.save(Store.builder().owner(owner).name("내 가게").category(StoreCategory.CAFE)
				.address("서울").location(GEOMETRY.createPoint(new Coordinate(126.95, 37.49))).build());

		as(owner, get("/api/v1/members/me"), null)
				.andExpect(jsonPath("$.data.role").value("OWNER"))
				.andExpect(jsonPath("$.data.email").doesNotExist())
				.andExpect(jsonPath("$.data.ownedStoreCount").value(1));
	}

	@Test
	void 로그인하지_않으면_UNAUTHORIZED다() throws Exception {
		mockMvc.perform(get("/api/v1/members/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void 푸시_토큰을_저장한다() throws Exception {
		as(customer, patch("/api/v1/members/me/fcm-token"), "{\"fcmToken\": \" token-abc \"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
		assertThat(memberRepository.findById(customer.getId())).get()
				.extracting(Member::getFcmToken).isEqualTo("token-abc");

		as(customer, patch("/api/v1/members/me/fcm-token"), "{\"fcmToken\": \"  \"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
	}

	@Test
	void 탈퇴하면_개인정보와_저장_토큰이_지워지고_예약_기록은_남는다() throws Exception {
		bookmarkRepository.save(Bookmark.builder().member(customer).store(store).build());
		refreshTokenRepository.save(RefreshToken.builder().member(customer).tokenHash("a".repeat(64))
				.expiresAt(LocalDateTime.now().plusDays(1)).build());
		Menu menu = menuRepository.save(Menu.builder().store(store).name("된장찌개").price(9000).build());
		ClosingDeal deal = closingDealRepository.save(ClosingDeal.builder().store(store).menu(menu)
				.originalPrice(9000).salePrice(6000).stock(3)
				.openAt(LocalDateTime.now().minusDays(1)).pickupEndAt(LocalDateTime.now().minusDays(1).plusHours(3))
				.build());
		Reservation reservation = reservationRepository.save(Reservation.builder().member(customer)
				.closingDeal(deal).quantity(1).build());

		as(customer, delete("/api/v1/members/me"), null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		Member withdrawn = memberRepository.findById(customer.getId()).orElseThrow();
		assertThat(withdrawn.isWithdrawn()).isTrue();
		assertThat(withdrawn.getNickname()).isEqualTo(Member.WITHDRAWN_NICKNAME);
		assertThat(withdrawn.getEmail()).isNull();
		assertThat(withdrawn.getProviderId()).isNotEqualTo("1001");
		assertThat(bookmarkRepository.findAllWithStoreByMemberId(customer.getId())).isEmpty();
		assertThat(refreshTokenRepository.findAll()).isEmpty();
		assertThat(reservationRepository.findById(reservation.getId())).isPresent();

		// 남은 accessToken으로는 내 정보를 볼 수 없다
		as(customer, get("/api/v1/members/me"), null)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
		as(customer, delete("/api/v1/members/me"), null)
				.andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
		// 같은 카카오 회원번호로 새로 가입할 수 있다 (unique (provider, provider_id))
		memberRepository.save(Member.builder().provider(AuthProvider.KAKAO).providerId("1001")
				.nickname("다시 가입").role(MemberRole.CONSUMER).build());
	}

	@Test
	void 가게가_연결된_사장님은_탈퇴할_수_없다() throws Exception {
		Member owner = memberRepository.save(Member.builder().provider(AuthProvider.KAKAO).providerId("3003")
				.nickname("사장님").role(MemberRole.OWNER).build());
		storeRepository.save(Store.builder().owner(owner).name("내 가게").category(StoreCategory.CAFE)
				.address("서울").location(GEOMETRY.createPoint(new Coordinate(126.95, 37.49))).build());

		as(owner, delete("/api/v1/members/me"), null)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("OWNER_HAS_STORE"));
		assertThat(memberRepository.findById(owner.getId())).get()
				.extracting(Member::isWithdrawn).isEqualTo(false);
	}

	private ResultActions as(Member member, MockHttpServletRequestBuilder request, String body) throws Exception {
		String token = jwtProvider.issueAccessToken(new AuthMember(member.getId(), member.getRole()));
		request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
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

}
