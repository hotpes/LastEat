package com.lasteat.store.repository;

import com.lasteat.store.entity.StoreImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

	// 가게별 첫 번째가 대표 사진
	@Query("select i from StoreImage i where i.store.id in :storeIds order by i.sortOrder, i.id")
	List<StoreImage> findAllByStoreIds(@Param("storeIds") Collection<Long> storeIds);

	@Query("select i from StoreImage i where i.store.id = :storeId order by i.sortOrder, i.id")
	List<StoreImage> findAllByStoreId(@Param("storeId") Long storeId);

	// 가게 id → 대표 사진 URL. 사진이 없는 가게는 키가 없다
	default Map<Long, String> findThumbnailUrls(Collection<Long> storeIds) {
		if (storeIds.isEmpty()) {
			return Map.of();
		}
		return findAllByStoreIds(storeIds).stream()
				.collect(Collectors.toMap(image -> image.getStore().getId(), StoreImage::getImageUrl,
						(first, ignored) -> first));
	}

}
