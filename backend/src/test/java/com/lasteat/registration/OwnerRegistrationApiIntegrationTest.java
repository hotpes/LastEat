package com.lasteat.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.lasteat.TestcontainersConfiguration;
import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.auth.jwt.JwtProvider;
import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.closingdeal.repository.ClosingDealRepository;
import com.lasteat.closingdeal.repository.ReservationRepository;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.registration.nts.BusinessInfo;
import com.lasteat.registration.nts.BusinessVerification;
import com.lasteat.registration.nts.NtsClient;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// 사장님 등록: 국세청 진위확인(목) → 가게 생성·사장님 전환·새 토큰
@SpringBootTest(properties = "lasteat.scheduler.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class OwnerRegistrationApiIntegrationTest {

	private static final String BODY = """
			{"businessNumber": "123-45-67890", "openDate": "2021-03-15", "representativeName": " 홍길동 ",
			 "storeName": " 연남 온기 ", "category": "KOREAN", "address": "서울 동작구 상도로 1",
			 "lat": 37.4972, "lng": 126.9575, "phone": "02-123-4567"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtProvider jwtProvider;

	@MockitoBean
	private NtsClient ntsClient;

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

	@BeforeEach
	void setUp() {
		cleanUp();
		customer = saveMember("1001");
	}

	@AfterEach
	void tearDown() {
		cleanUp();
	}

	@Test
	void 진위확인을_통과하면_가게를_만들고_사장님_토큰을_준다() throws Exception {
		given(ntsClient.verify(any())).willReturn(BusinessVerification.ACTIVE);

		String body = register(customer, BODY)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.storeId").isNumber())
				.andExpect(jsonPath("$.data.accessToken").isString())
				.andExpect(jsonPath("$.data.refreshToken").isString())
				.andReturn().getResponse().getContentAsString();

		verify(ntsClient).verify(new BusinessInfo("1234567890", LocalDate.of(2021, 3, 15), "홍길동"));
		assertThat(memberRepository.findById(customer.getId())).get()
				.extracting(Member::getRole).isEqualTo(MemberRole.OWNER);
		Number storeId = JsonPath.read(body, "$.data.storeId");
		Store store = storeRepository.findById(storeId.longValue()).orElseThrow();
		assertThat(store.getBusinessNumber()).isEqualTo("1234567890");
		assertThat(store.getName()).isEqualTo("연남 온기");
		assertThat(store.getPhone()).isEqualTo("02-123-4567");

		// 새 토큰으로 바로 사장님 API를 쓸 수 있고, 영업시간 입력 전이라 CLOSED
		String accessToken = JsonPath.read(body, "$.data.accessToken");
		mockMvc.perform(get("/api/v1/owner/stores/{id}", storeId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("연남 온기"))
				.andExpect(jsonPath("$.data.businessStatus").value("CLOSED"));
		mockMvc.perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(jsonPath("$.data.role").value("OWNER"))
				.andExpect(jsonPath("$.data.ownedStoreCount").value(1));
	}

	@Test
	void 국세청_정보와_다르면_BUSINESS_NOT_VERIFIED이고_아무것도_바뀌지_않는다() throws Exception {
		given(ntsClient.verify(any())).willReturn(BusinessVerification.NOT_MATCHED);

		register(customer, BODY)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("BUSINESS_NOT_VERIFIED"));
		assertNothingChanged();
	}

	@Test
	void 휴업_폐업이면_BUSINESS_NOT_ACTIVE다() throws Exception {
		given(ntsClient.verify(any())).willReturn(BusinessVerification.NOT_ACTIVE);

		register(customer, BODY)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("BUSINESS_NOT_ACTIVE"));
		assertNothingChanged();
	}

	@Test
	void 국세청을_쓸_수_없으면_BUSINESS_VERIFICATION_UNAVAILABLE이다() throws Exception {
		given(ntsClient.verify(any()))
				.willThrow(new BusinessException(ErrorCode.BUSINESS_VERIFICATION_UNAVAILABLE));

		register(customer, BODY)
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.error.code").value("BUSINESS_VERIFICATION_UNAVAILABLE"));
		assertNothingChanged();
	}

	@Test
	void 이미_등록된_사업자번호면_국세청에_묻지_않고_DUPLICATE_BUSINESS_NUMBER다() throws Exception {
		given(ntsClient.verify(any())).willReturn(BusinessVerification.ACTIVE);
		register(customer, BODY).andExpect(status().isOk());
		Member another = saveMember("2002");

		register(another, BODY.replace("123-45-67890", "1234567890"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("DUPLICATE_BUSINESS_NUMBER"));
		verify(ntsClient).verify(any());
		assertThat(memberRepository.findById(another.getId())).get()
				.extracting(Member::getRole).isEqualTo(MemberRole.CONSUMER);
	}

	@Test
	void 입력값이_잘못되면_국세청에_묻지_않고_INVALID_INPUT이다() throws Exception {
		String[] bodies = {
				BODY.replace("123-45-67890", "123-45-6789"),
				BODY.replace("123-45-67890", "abc-45-67890"),
				BODY.replace("\"2021-03-15\"", "\"2999-01-01\""),
				BODY.replace("\"2021-03-15\"", "\"2021\""),
				BODY.replace(" 홍길동 ", "  "),
				BODY.replace(" 연남 온기 ", "　"),
				BODY.replace("\"KOREAN\"", "\"PIZZA\""),
				BODY.replace("37.4972", "91"),
				BODY.replace("02-123-4567", "02)123"),
		};
		for (String body : bodies) {
			register(customer, body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
		}
		verify(ntsClient, never()).verify(any());
	}

	@Test
	void 로그인하지_않으면_UNAUTHORIZED다() throws Exception {
		mockMvc.perform(post("/api/v1/owner/registration").contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isUnauthorized());
	}

	private void assertNothingChanged() {
		assertThat(storeRepository.findAll()).isEmpty();
		assertThat(memberRepository.findById(customer.getId())).get()
				.extracting(Member::getRole).isEqualTo(MemberRole.CONSUMER);
		assertThat(refreshTokenRepository.findAll()).isEmpty();
	}

	private ResultActions register(Member member, String body) throws Exception {
		String token = jwtProvider.issueAccessToken(new AuthMember(member.getId(), member.getRole()));
		return mockMvc.perform(post("/api/v1/owner/registration")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private Member saveMember(String providerId) {
		return memberRepository.save(Member.builder().provider(AuthProvider.KAKAO).providerId(providerId)
				.nickname(providerId).role(MemberRole.CONSUMER).build());
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
