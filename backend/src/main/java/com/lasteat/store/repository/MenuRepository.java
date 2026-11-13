package com.lasteat.store.repository;

import com.lasteat.store.entity.Menu;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long> {

	@Query("select m from Menu m where m.store.id in :storeIds and m.signature = true order by m.id")
	List<Menu> findSignaturesByStoreIds(@Param("storeIds") Collection<Long> storeIds);

	// 대표 메뉴 먼저, 그다음 등록 순
	@Query("select m from Menu m where m.store.id = :storeId order by m.signature desc, m.id")
	List<Menu> findAllByStoreId(@Param("storeId") Long storeId);

}
