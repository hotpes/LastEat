package com.lasteat.benefit.repository;

import com.lasteat.benefit.entity.Benefit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BenefitRepository extends JpaRepository<Benefit, Long> {

	// 손님에게 보이는(공개 중인) 할인 공지, 최근 게시 순
	@Query("""
			select b from Benefit b
			where b.store.id in :storeIds and b.published = true
			order by b.publishedAt desc, b.id desc
			""")
	List<Benefit> findPublishedByStoreIds(@Param("storeIds") Collection<Long> storeIds);

	// 사장님용: 비공개까지 전부, 최근 게시 순
	@Query("select b from Benefit b where b.store.id = :storeId order by b.publishedAt desc, b.id desc")
	List<Benefit> findAllByStoreIdLatestFirst(@Param("storeId") Long storeId);

	// 반경 안 가게의 공개 공지. 가까운 순, 같은 가게면 최근 게시 순.
	// 공간 인덱스(MBRContains + 사각형)로 가게를 먼저 거른다. 좌표 WKT는 GeoPoint가 만든 숫자 문자열이다.
	@Query(nativeQuery = true, value = """
			SELECT b.id AS id,
			       ST_Distance_Sphere(s.location, ST_GeomFromText(:point, 4326, 'axis-order=long-lat')) AS distance
			FROM benefit b
			JOIN store s ON s.id = b.store_id
			WHERE b.published = TRUE
			  AND (:partnerOnly = FALSE OR b.partner = TRUE)
			  AND MBRContains(ST_GeomFromText(:box, 4326, 'axis-order=long-lat'), s.location)
			  AND ST_Distance_Sphere(s.location, ST_GeomFromText(:point, 4326, 'axis-order=long-lat')) <= :radius
			ORDER BY distance, b.published_at DESC, b.id DESC
			LIMIT :limit
			""")
	List<BenefitDistance> findNearbyPublished(
			@Param("point") String pointWkt,
			@Param("box") String boundingBoxWkt,
			@Param("radius") int radiusMeters,
			@Param("partnerOnly") boolean partnerOnly,
			@Param("limit") int limit);

	@Query("select b from Benefit b join fetch b.store where b.id in :ids")
	List<Benefit> findAllWithStoreByIdIn(@Param("ids") Collection<Long> ids);

	// 다른 가게의 공지 id로 수정·삭제하지 못하도록 가게 id와 함께 찾는다
	@Query("select b from Benefit b where b.id = :id and b.store.id = :storeId")
	Optional<Benefit> findByIdAndStoreId(@Param("id") Long id, @Param("storeId") Long storeId);

	interface BenefitDistance {

		Long getId();

		Double getDistance();

	}

}
