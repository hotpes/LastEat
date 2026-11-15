package com.lasteat.benefit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.benefit.entity.Benefit;
import static com.lasteat.benefit.entity.BenefitFixture.PUBLISHED_AT;

import com.lasteat.benefit.entity.BenefitFixture;
import com.lasteat.global.config.JpaAuditingConfig;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

// 할인 공지 조회 순서와 목록형 값(쉼표 구분 문자열) 저장을 실제 MySQL로 확인한다.
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
class BenefitRepositoryTest {

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private BenefitRepository benefitRepository;

	@Autowired
	private EntityManager em;

	@Test
	void 공개_중인_할인_공지만_최근_게시_순으로_찾는다() {
		Store store = storeRepository.save(newStore());
		Benefit older = benefitRepository.save(BenefitFixture.notice(store, "예전 공지", PUBLISHED_AT));
		Benefit newer = benefitRepository.save(BenefitFixture.notice(store, "새 공지", PUBLISHED_AT.plusDays(1)));
		Benefit hidden = BenefitFixture.notice(store, "비공개 공지", PUBLISHED_AT.plusDays(2));
		hidden.changePublished(false);
		benefitRepository.saveAndFlush(hidden);
		em.clear();

		List<Benefit> published = benefitRepository.findPublishedByStoreIds(List.of(store.getId()));
		List<Benefit> all = benefitRepository.findAllByStoreIdLatestFirst(store.getId());

		assertThat(published).extracting(Benefit::getId).containsExactly(newer.getId(), older.getId());
		assertThat(all).extracting(Benefit::getTitle).containsExactly("비공개 공지", "새 공지", "예전 공지");
	}

	@Test
	void 할인_공지는_가게_id와_함께_찾는다() {
		Store store = storeRepository.save(newStore());
		Store other = storeRepository.save(newStore());
		Benefit benefit = benefitRepository.saveAndFlush(BenefitFixture.notice(store, "공지"));

		assertThat(benefitRepository.findByIdAndStoreId(benefit.getId(), store.getId())).isPresent();
		assertThat(benefitRepository.findByIdAndStoreId(benefit.getId(), other.getId())).isEmpty();
	}

	@Test
	void 가게의_휴무일과_태그는_문자열로_저장되고_다시_읽힌다() {
		Long id = storeRepository.saveAndFlush(newStore()).getId();
		em.clear();

		Store found = storeRepository.findById(id).orElseThrow();

		assertThat(found.getClosedDays()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.SUNDAY);
		assertThat(found.getTags()).containsExactly("혼밥 좋아요", "포장 가능");
	}

	@Test
	void 휴무일과_태그가_없으면_빈_목록으로_읽힌다() {
		Store store = Store.builder()
				.name("가게")
				.category(StoreCategory.CAFE)
				.address("서울 동작구 상도로 369")
				.location(GEOMETRY_FACTORY.createPoint(new Coordinate(126.9575, 37.4963)))
				.build();
		Long id = storeRepository.saveAndFlush(store).getId();
		em.clear();

		Store found = storeRepository.findById(id).orElseThrow();

		assertThat(found.getClosedDays()).isEmpty();
		assertThat(found.getTags()).isEmpty();
	}

	private static Store newStore() {
		return Store.builder()
				.name("연남 온기")
				.category(StoreCategory.KOREAN)
				.address("서울 동작구 상도로 369")
				.location(GEOMETRY_FACTORY.createPoint(new Coordinate(126.9575, 37.4963)))
				.closedDays(Set.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY))
				.tags(List.of("혼밥 좋아요", "포장 가능"))
				.build();
	}

}
