package com.lasteat.store.service;

import com.lasteat.benefit.entity.Benefit;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.store.dto.StoreSummaryResponse;
import com.lasteat.store.dto.StoreSummaryResponse.MainBenefit;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 가게 목록 카드에 붙는 대표 사진·대표 메뉴·대표 혜택·저장 여부를 한 페이지 단위로 한 번에 조회한다 (가게마다 쿼리하지 않음).
@Component
@RequiredArgsConstructor
public class StoreSummaryAssembler {

	private final StoreImageRepository storeImageRepository;

	private final MenuRepository menuRepository;

	private final BenefitRepository benefitRepository;

	private final BookmarkRepository bookmarkRepository;

	// memberId가 null이면 저장 여부는 모두 false
	public List<StoreSummaryResponse> assemble(List<StoreWithDistance> stores, Long memberId, LocalDateTime now) {
		if (stores.isEmpty()) {
			return List.of();
		}
		List<Long> storeIds = stores.stream().map(item -> item.store().getId()).toList();
		Map<Long, String> thumbnails = storeImageRepository.findThumbnailUrls(storeIds);
		Map<Long, String> signatureMenus = firstByStore(menuRepository.findSignaturesByStoreIds(storeIds),
				menu -> menu.getStore().getId(), Menu::getName);
		// 가게별 가장 최근에 게시한 공개 공지
		Map<Long, Benefit> mainBenefits = firstByStore(benefitRepository.findPublishedByStoreIds(storeIds),
				benefit -> benefit.getStore().getId(), Function.identity());
		Set<Long> bookmarked = memberId == null
				? Set.of()
				: new HashSet<>(bookmarkRepository.findStoreIdsByMemberIdAndStoreIds(memberId, storeIds));

		return stores.stream()
				.map(item -> toResponse(item, thumbnails, signatureMenus, mainBenefits, bookmarked, now))
				.toList();
	}

	private static StoreSummaryResponse toResponse(StoreWithDistance item, Map<Long, String> thumbnails,
			Map<Long, String> signatureMenus, Map<Long, Benefit> mainBenefits, Set<Long> bookmarked,
			LocalDateTime now) {
		Store store = item.store();
		GeoPoint location = GeoPoint.from(store.getLocation());
		Benefit benefit = mainBenefits.get(store.getId());
		return new StoreSummaryResponse(
				store.getId(),
				store.getName(),
				store.getCategory(),
				signatureMenus.get(store.getId()),
				thumbnails.get(store.getId()),
				location.lat(),
				location.lng(),
				item.distance(),
				store.businessStatusAt(now),
				store.isRecommended(),
				benefit == null ? null : new MainBenefit(benefit.getId(), benefit.getTitle()),
				bookmarked.contains(store.getId()));
	}

	// 정렬된 목록에서 가게별 첫 항목만 남긴다
	private static <T, V> Map<Long, V> firstByStore(List<T> rows, Function<T, Long> storeId, Function<T, V> value) {
		return rows.stream().collect(Collectors.toMap(storeId, value, (first, ignored) -> first, LinkedHashMap::new));
	}

}
