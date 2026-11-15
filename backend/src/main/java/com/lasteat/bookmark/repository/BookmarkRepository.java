package com.lasteat.bookmark.repository;

import com.lasteat.bookmark.entity.Bookmark;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	boolean existsByMemberIdAndStoreId(Long memberId, Long storeId);

	@Query("select b.store.id from Bookmark b where b.member.id = :memberId and b.store.id in :storeIds")
	List<Long> findStoreIdsByMemberIdAndStoreIds(@Param("memberId") Long memberId,
			@Param("storeIds") Collection<Long> storeIds);

	@Query("select b from Bookmark b join fetch b.store where b.member.id = :memberId order by b.createdAt desc, b.id desc")
	List<Bookmark> findAllWithStoreByMemberId(@Param("memberId") Long memberId);

	// 이미 저장한 가게면 아무것도 바꾸지 않는다. 동시에 두 번 눌러도 유니크 제약 오류 없이 1건만 남는다.
	@Modifying
	@Query(nativeQuery = true, value = """
			INSERT INTO bookmark (member_id, store_id, created_at, updated_at)
			VALUES (:memberId, :storeId, :now, :now)
			ON DUPLICATE KEY UPDATE id = id
			""")
	void insertIfAbsent(@Param("memberId") Long memberId, @Param("storeId") Long storeId,
			@Param("now") LocalDateTime now);

	@Modifying
	@Query("delete from Bookmark b where b.member.id = :memberId and b.store.id = :storeId")
	int deleteByMemberIdAndStoreId(@Param("memberId") Long memberId, @Param("storeId") Long storeId);

}
