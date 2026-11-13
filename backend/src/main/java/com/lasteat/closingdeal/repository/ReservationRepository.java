package com.lasteat.closingdeal.repository;

import com.lasteat.closingdeal.entity.Reservation;
import com.lasteat.closingdeal.entity.ReservationStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	// 이 손님이 이 마감할인을 수령 대기(RESERVED) 중인 예약 id
	@Query("""
			select r.id from Reservation r
			where r.member.id = :memberId and r.closingDeal.id = :dealId
			  and r.status = com.lasteat.closingdeal.entity.ReservationStatus.RESERVED
			""")
	List<Long> findReservedIds(@Param("memberId") Long memberId, @Param("dealId") Long dealId);

	// 예약 상세·내 예약 목록·사장님 처리에 필요한 연관을 함께 읽는다
	@Query("""
			select r from Reservation r
			join fetch r.member join fetch r.closingDeal d join fetch d.menu join fetch d.store
			where r.id = :id
			""")
	Optional<Reservation> findWithDeal(@Param("id") Long id);

	// 내 예약, 최근 예약 순
	@Query(value = """
			select r from Reservation r join fetch r.closingDeal d join fetch d.menu join fetch d.store
			where r.member.id = :memberId and r.status in :statuses
			order by r.id desc
			""",
			countQuery = "select count(r) from Reservation r where r.member.id = :memberId and r.status in :statuses")
	Page<Reservation> findMine(@Param("memberId") Long memberId,
			@Param("statuses") Collection<ReservationStatus> statuses, Pageable pageable);

	// 사장님용: 노출 시작이 [from, to)인 마감할인의 예약. 수령 대기 먼저, 그다음 예약 순
	@Query(value = """
			select r from Reservation r join fetch r.member join fetch r.closingDeal d join fetch d.menu
			where d.store.id = :storeId and d.openAt >= :from and d.openAt < :to
			  and (:dealId is null or d.id = :dealId) and r.status in :statuses
			order by case when r.status = com.lasteat.closingdeal.entity.ReservationStatus.RESERVED then 0 else 1 end,
			         r.id
			""",
			countQuery = """
					select count(r) from Reservation r join r.closingDeal d
					where d.store.id = :storeId and d.openAt >= :from and d.openAt < :to
					  and (:dealId is null or d.id = :dealId) and r.status in :statuses
					""")
	Page<Reservation> findForStore(@Param("storeId") Long storeId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to, @Param("dealId") Long dealId,
			@Param("statuses") Collection<ReservationStatus> statuses, Pageable pageable);

	// 마감할인별·상태별 수량 합계 (사장님 목록의 예약·수령 수량)
	@Query("""
			select r.closingDeal.id as dealId, r.status as status, sum(r.quantity) as quantity
			from Reservation r
			where r.closingDeal.id in :dealIds
			group by r.closingDeal.id, r.status
			""")
	List<QuantityByStatus> sumQuantities(@Param("dealIds") Collection<Long> dealIds);

	// 수령 마감이 cutoff 이전인 마감할인(조기 마감 포함)의 수령 대기 예약을 노쇼로 (스케줄러용).
	// version을 올려서 동시에 진행 중인 취소·수령 처리가 낙관적 락으로 실패하게 한다.
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			update Reservation r
			set r.status = com.lasteat.closingdeal.entity.ReservationStatus.NO_SHOW,
			    r.updatedAt = :now, r.version = r.version + 1
			where r.status = com.lasteat.closingdeal.entity.ReservationStatus.RESERVED
			  and r.closingDeal.id in (select d.id from ClosingDeal d where d.pickupEndAt <= :cutoff)
			""")
	int markNoShowPickupEndedBefore(@Param("cutoff") LocalDateTime cutoff, @Param("now") LocalDateTime now);

	interface QuantityByStatus {

		Long getDealId();

		ReservationStatus getStatus();

		Long getQuantity();

	}

}
