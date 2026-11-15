package com.lasteat.store.repository;

import com.lasteat.store.entity.Store;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long> {

	// 사장님 가게 목록, 등록 순
	@Query("select s from Store s where s.owner.id = :ownerId order by s.id")
	List<Store> findAllByOwnerId(@Param("ownerId") Long ownerId);

	// 반경 안 가게를 가까운 순으로. 공간 인덱스(MBRContains + 사각형)로 먼저 거르고 실제 거리로 다시 거른다.
	// 지금 영업 중 필터는 영업시간 계산이 복잡해서 서비스(Store.businessStatusAt)에서 한다.
	// 좌표 WKT는 GeoPoint가 만든 숫자 문자열이고 나머지 값은 모두 바인딩 파라미터다.
	@Query(nativeQuery = true, value = """
			SELECT s.id AS id,
			       ST_Distance_Sphere(s.location, ST_GeomFromText(:point, 4326, 'axis-order=long-lat')) AS distance
			FROM store s
			WHERE MBRContains(ST_GeomFromText(:box, 4326, 'axis-order=long-lat'), s.location)
			  AND ST_Distance_Sphere(s.location, ST_GeomFromText(:point, 4326, 'axis-order=long-lat')) <= :radius
			  AND s.category IN (:categories)
			  AND (:keyword IS NULL
			       OR s.name LIKE :keyword
			       OR s.category IN (:keywordCategories)
			       OR EXISTS (SELECT 1 FROM menu m WHERE m.store_id = s.id AND m.name LIKE :keyword))
			  AND (:hasBenefit = FALSE
			       OR EXISTS (SELECT 1 FROM benefit b WHERE b.store_id = s.id AND b.published = TRUE))
			ORDER BY distance, s.id
			LIMIT :limit
			""")
	List<StoreDistance> findNearby(
			@Param("point") String pointWkt,
			@Param("box") String boundingBoxWkt,
			@Param("radius") int radiusMeters,
			@Param("categories") Collection<String> categories,
			@Param("keyword") String keywordPattern,
			@Param("keywordCategories") Collection<String> keywordCategories,
			@Param("hasBenefit") boolean hasBenefit,
			@Param("limit") int limit);

	interface StoreDistance {

		Long getId();

		Double getDistance();

	}

}
