package com.lasteat.closingdeal;

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
import java.time.ZoneId;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// 마감할인 통합 테스트 공통 준비. 지금은 2026-11-03(화) 20:30으로 고정한다.
abstract class ClosingDealTestSupport {

	static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 20, 30);

	static final double LAT = 37.4963;

	static final double LNG = 126.9575;

	private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

	@Autowired
	JwtProvider jwtProvider;

	@Autowired
	StoreRepository storeRepository;

	@Autowired
	StoreImageRepository storeImageRepository;

	@Autowired
	MenuRepository menuRepository;

	@Autowired
	BenefitRepository benefitRepository;

	@Autowired
	BookmarkRepository bookmarkRepository;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	RefreshTokenRepository refreshTokenRepository;

	@Autowired
	ClosingDealRepository closingDealRepository;

	@Autowired
	ReservationRepository reservationRepository;

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClockConfig {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(NOW.atZone(SEOUL).toInstant(), SEOUL);
		}

	}

	void cleanUp() {
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

	Member saveMember(String providerId, MemberRole role) {
		return memberRepository.save(Member.builder()
				.provider(AuthProvider.KAKAO).providerId(providerId).nickname(providerId).role(role).build());
	}

	String tokenOf(Member member) {
		return jwtProvider.issueAccessToken(new AuthMember(member.getId(), member.getRole()));
	}

	Store saveStore(Member owner, String name, StoreCategory category, double lat, double lng) {
		return storeRepository.save(Store.builder()
				.owner(owner).name(name).category(category).address("서울 동작구 상도로 " + name)
				.phone("02-000-0000")
				.location(GEOMETRY.createPoint(new Coordinate(lng, lat)))
				.build());
	}

	Menu saveMenu(Store store, String name, int price) {
		return menuRepository.save(Menu.builder().store(store).name(name).price(price)
				.description(name + " 설명").build());
	}

	ClosingDeal saveDeal(Menu menu, int salePrice, int stock, LocalDateTime openAt, LocalDateTime pickupEndAt) {
		return closingDealRepository.save(ClosingDeal.builder()
				.store(menu.getStore()).menu(menu)
				.originalPrice(menu.getPrice()).salePrice(salePrice).stock(stock)
				.openAt(openAt).pickupEndAt(pickupEndAt)
				.build());
	}

}
