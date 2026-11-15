package com.lasteat.store.service;

import com.lasteat.store.dto.MenuResponse;
import com.lasteat.store.dto.OwnerStoreDetailResponse;
import com.lasteat.store.dto.OwnerStoreSummaryResponse;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStoreQueryService {

	private final StoreRepository storeRepository;

	private final StoreImageRepository storeImageRepository;

	private final MenuRepository menuRepository;

	private final OwnedStoreReader ownedStoreReader;

	private final StoreQueryService storeQueryService;

	private final Clock clock;

	public List<OwnerStoreSummaryResponse> getMyStores(Long memberId) {
		List<Store> stores = storeRepository.findAllByOwnerId(memberId);
		if (stores.isEmpty()) {
			return List.of();
		}
		LocalDateTime now = LocalDateTime.now(clock);
		Map<Long, String> thumbnails = storeImageRepository.findThumbnailUrls(stores.stream().map(Store::getId)
				.toList());
		return stores.stream()
				.map(store -> new OwnerStoreSummaryResponse(store.getId(), store.getName(), store.getAddress(),
						thumbnails.get(store.getId()), store.businessStatusAt(now)))
				.toList();
	}

	public OwnerStoreDetailResponse getMyStore(Long storeId, Long memberId) {
		Store store = ownedStoreReader.read(storeId, memberId);
		// 영업 상태와 오늘 정한 상태가 자정 경계에서 어긋나지 않도록 같은 시각으로 계산한다
		LocalDateTime now = LocalDateTime.now(clock);
		// 사장님 화면에는 거리·저장 여부가 필요 없다
		var detail = storeQueryService.getDetail(storeId, Optional.empty(), null, now);
		var override = store.businessStatusOverrideOn(now.toLocalDate()).orElse(null);
		return OwnerStoreDetailResponse.of(detail, override);
	}

	public List<MenuResponse> getMenus(Long storeId, Long memberId) {
		ownedStoreReader.read(storeId, memberId);
		return menuRepository.findAllByStoreId(storeId).stream().map(MenuResponse::from).toList();
	}

}
