package com.lasteat.closingdeal.repository;

import com.lasteat.closingdeal.entity.ClosingDeal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// "손님에게 보이는" 조건은 ClosingDeal.isVisibleAt과 같다: 조기 마감 아님 + 노출 시작 지남 + 수령 마감 전
public interface ClosingDealRepository extends JpaRepository<ClosingDeal, Long> {

	// 반경 안 가게의 노출 중인 마감할인. 기본 가까운 순, endingSoon이면 수령 마감 가까운 순.
	// 공간 인덱스(MBRContains + 사각형)로 가게를 먼저 거른다. 좌표 WKT는 GeoPoint가 만든 숫자 문자열이다.
	@Query(nativeQuery = true, value = """
			SELECT d.id AS id,
			       ST_Distance_Sphere(s.location, ST_GeomFromText(:point, 4326, 'axis-order=long-lat')) AS distance
			FROM closing_deal d
			JOIN store s ON s.id = d.store_id
			WHERE d.status <> 'CLOSED' AND d.open_at <= :now AND d.pickup_end_at > :now
			  AND (:availableOnly = FALSE OR d.stock > 0)
			  AND s.category IN (:categories)
			  AND MBRContains(ST_GeomFromText(:box, 4326, 'axis-order=long-lat'), s.location)
			  AND ST_Distance_Sphere(s.location, ST_GeomFromText(:point, 4326, 'axis-order=long-lat')) <= :radius
			ORDER BY CASE WHEN :endingSoon = TRUE THEN d.pickup_end_at END, distance, d.pickup_end_at, d.id
			LIMIT :limit
			""")
	List<DealDistance> findNearbyVisible(
			@Param("point") String pointWkt,
			@Param("box") String boundingBoxWkt,
			@Param("radius") int radiusMeters,
			@Param("categories") Collection<String> categories,
			@Param("availableOnly") boolean availableOnly,
			@Param("endingSoon") boolean endingSoon,
			@Param("now") LocalDateTime now,
			@Param("limit") int limit);

	@Query("select d from ClosingDeal d join fetch d.menu join fetch d.store where d.id in :ids")
	List<ClosingDeal> findAllWithMenuAndStoreByIdIn(@Param("ids") Collection<Long> ids);

	default Optional<ClosingDeal> findWithMenuAndStore(Long id) {
		return findAllWithMenuAndStoreByIdIn(List.of(id)).stream().findFirst();
	}

	// 가게의 노출 중인 마감할인, 수령 마감 가까운 순
	@Query("""
			select d from ClosingDeal d join fetch d.menu join fetch d.store
			where d.store.id = :storeId
			  and d.status <> com.lasteat.closingdeal.entity.ClosingDealStatus.CLOSED
			  and d.openAt <= :now and d.pickupEndAt > :now
			order by d.pickupEndAt, d.id
			""")
	List<ClosingDeal> findVisibleByStoreId(@Param("storeId") Long storeId, @Param("now") LocalDateTime now);

	// 사장님용: 노출 시작이 [from, to)인 마감할인, 노출 시작 순
	@Query("""
			select d from ClosingDeal d join fetch d.menu
			where d.store.id = :storeId and d.openAt >= :from and d.openAt < :to
			order by d.openAt, d.id
			""")
	List<ClosingDeal> findAllByStoreIdAndOpenAtIn(@Param("storeId") Long storeId,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("select d from ClosingDeal d join fetch d.menu where d.id = :id and d.store.id = :storeId")
	Optional<ClosingDeal> findByIdAndStoreId(@Param("id") Long id, @Param("storeId") Long storeId);

	// 수령 마감이 지났는데 아직 마감 처리되지 않은 것 (스케줄러용)
	@Query("""
			select d from ClosingDeal d
			where d.status <> com.lasteat.closingdeal.entity.ClosingDealStatus.CLOSED and d.pickupEndAt <= :now
			""")
	List<ClosingDeal> findExpiredNotClosed(@Param("now") LocalDateTime now);

	interface DealDistance {

		Long getId();

		Double getDistance();

	}

}
