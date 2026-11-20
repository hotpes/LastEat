package com.lasteat.store.repository;

import com.lasteat.store.entity.Menu;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 삭제 표시(deleted)된 메뉴는 조회에서 뺀다. findById는 지난 마감할인 확인용으로만 쓴다.
public interface MenuRepository extends JpaRepository<Menu, Long> {

	@Query("select m from Menu m where m.store.id in :storeIds and m.signature = true and m.deleted = false"
			+ " order by m.id")
	List<Menu> findSignaturesByStoreIds(@Param("storeIds") Collection<Long> storeIds);

	// 대표 메뉴 먼저, 그다음 등록 순
	@Query("select m from Menu m where m.store.id = :storeId and m.deleted = false order by m.signature desc, m.id")
	List<Menu> findAllByStoreId(@Param("storeId") Long storeId);

	// 다른 가게 메뉴나 삭제된 메뉴는 없는 것으로 본다
	@Query("select m from Menu m where m.id = :menuId and m.store.id = :storeId and m.deleted = false")
	Optional<Menu> findActiveByIdAndStoreId(@Param("menuId") Long menuId, @Param("storeId") Long storeId);

}
