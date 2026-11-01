package com.lasteat.store.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.global.config.JpaAuditingConfig;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
class StoreRepositoryTest {

	// 숭실대학교 정문 부근
	private static final double LATITUDE = 37.4963;
	private static final double LONGITUDE = 126.9575;

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private EntityManager em;

	@Test
	void 좌표를_저장하면_MySQL에서_위도와_경도가_뒤집히지_않는다() {
		Store saved = storeRepository.saveAndFlush(storeAt(LATITUDE, LONGITUDE));

		Object[] latLng = (Object[]) em.createNativeQuery(
						"SELECT ST_Latitude(location), ST_Longitude(location) FROM store WHERE id = :id")
				.setParameter("id", saved.getId())
				.getSingleResult();

		assertThat(((Number) latLng[0]).doubleValue()).isCloseTo(LATITUDE, within(1e-9));
		assertThat(((Number) latLng[1]).doubleValue()).isCloseTo(LONGITUDE, within(1e-9));
	}

	@Test
	void 저장한_좌표를_다시_읽으면_x는_경도_y는_위도다() {
		Long id = storeRepository.saveAndFlush(storeAt(LATITUDE, LONGITUDE)).getId();
		em.clear();

		Point location = storeRepository.findById(id).orElseThrow().getLocation();

		assertThat(location.getX()).isCloseTo(LONGITUDE, within(1e-9));
		assertThat(location.getY()).isCloseTo(LATITUDE, within(1e-9));
		assertThat(location.getSRID()).isEqualTo(4326);
	}

	private static Store storeAt(double latitude, double longitude) {
		Point location = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
		return Store.builder()
				.name("테스트 가게")
				.category(StoreCategory.KOREAN)
				.address("서울 동작구 상도로 369")
				.location(location)
				.build();
	}

}
