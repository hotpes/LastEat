package com.lasteat.store.service;

import com.lasteat.benefit.entity.Benefit;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.global.converter.DayOfWeekSetConverter;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.global.response.PageResponse;
import com.lasteat.store.dto.MenuResponse;
import com.lasteat.store.dto.StoreDetailResponse;
import com.lasteat.store.dto.StoreDetailResponse.BenefitSummary;
import com.lasteat.store.dto.StoreDetailResponse.BusinessHours;
import com.lasteat.store.dto.StoreDetailResponse.SignatureMenu;
import com.lasteat.store.dto.StoreSearchRequest;
import com.lasteat.store.dto.StoreSummaryResponse;
import com.lasteat.store.entity.BusinessStatus;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.entity.StoreImage;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import com.lasteat.store.repository.StoreRepository.StoreDistance;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreQueryService {

	// 반경 3km 안 후보 상한. 1차 범위(학교 주변 상권)에서는 넘지 않는다. 넘으면 먼 가게부터 빠진다.
	static final int MAX_CANDIDATES = 1000;

	// IN ()은 SQL 오류라서 비교할 값이 없을 때 넣는 값
	private static final List<String> NO_MATCH = List.of("-");

	private final StoreRepository storeRepository;

	private final StoreImageRepository storeImageRepository;

	private final MenuRepository menuRepository;

	private final BenefitRepository benefitRepository;

	private final BookmarkRepository bookmarkRepository;

	private final StoreSummaryAssembler storeSummaryAssembler;

	private final Clock clock;

	public PageResponse<StoreSummaryResponse> search(StoreSearchRequest request, Long memberId) {
		LocalDateTime now = LocalDateTime.now(clock);
		List<StoreWithDistance> matched = findNearby(request, now).stream()
				.filter(item -> !request.openNow() || item.store().businessStatusAt(now) == BusinessStatus.OPEN)
				.toList();
		List<StoreWithDistance> pageItems = PageResponse.slice(matched, request.page(), request.size());
		return PageResponse.of(storeSummaryAssembler.assemble(pageItems, memberId, now), request.page(),
				request.size(), matched.size());
	}

	public StoreDetailResponse getDetail(Long storeId, Optional<GeoPoint> origin, Long memberId) {
		return getDetail(storeId, origin, memberId, LocalDateTime.now(clock));
	}

	// 같은 요청 안에서 다른 값과 같은 시각을 기준으로 계산해야 할 때 now를 넘긴다
	public StoreDetailResponse getDetail(Long storeId, Optional<GeoPoint> origin, Long memberId,
			LocalDateTime now) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
		GeoPoint location = GeoPoint.from(store.getLocation());
		List<Menu> menus = menuRepository.findAllByStoreId(storeId);
		List<Benefit> benefits = benefitRepository.findPublishedByStoreIds(List.of(storeId));
		return new StoreDetailResponse(
				store.getId(),
				store.getName(),
				store.getCategory(),
				store.getDescription(),
				store.getAddress(),
				store.getPhone(),
				location.lat(),
				location.lng(),
				origin.map(point -> point.distanceMetersTo(location)).orElse(null),
				storeImageRepository.findAllByStoreId(storeId).stream().map(StoreImage::getImageUrl).toList(),
				store.getTags(),
				store.isRecommended(),
				store.businessStatusAt(now),
				businessHoursOf(store),
				menus.stream().filter(Menu::isSignature).map(StoreQueryService::toSignatureMenu).toList(),
				menus.size(),
				benefits.stream().map(StoreQueryService::toBenefitSummary).toList(),
				memberId != null && bookmarkRepository.existsByMemberIdAndStoreId(memberId, storeId));
	}

	public List<MenuResponse> getMenus(Long storeId) {
		if (!storeRepository.existsById(storeId)) {
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}
		return menuRepository.findAllByStoreId(storeId).stream().map(MenuResponse::from).toList();
	}

	private List<StoreWithDistance> findNearby(StoreSearchRequest request, LocalDateTime now) {
		GeoPoint center = new GeoPoint(request.lat(), request.lng());
		String keyword = request.keyword();
		List<StoreDistance> rows = storeRepository.findNearby(
				center.toWkt(),
				center.boundingBoxWkt(request.radius()),
				request.radius(),
				categoryNames(request.category().isEmpty() ? Arrays.asList(StoreCategory.values()) : request.category()),
				keyword == null ? null : "%" + escapeLike(keyword) + "%",
				keyword == null ? NO_MATCH : categoryNames(StoreCategory.matchingKeyword(keyword)),
				request.hasBenefit(),
				MAX_CANDIDATES);
		Map<Long, Store> stores = storeRepository.findAllById(rows.stream().map(StoreDistance::getId).toList())
				.stream()
				.collect(Collectors.toMap(Store::getId, Function.identity()));
		return rows.stream()
				.map(row -> new StoreWithDistance(stores.get(row.getId()), (int) Math.round(row.getDistance())))
				.toList();
	}

	private static List<String> categoryNames(List<StoreCategory> categories) {
		return categories.isEmpty() ? NO_MATCH : categories.stream().map(Enum::name).toList();
	}

	// 검색어의 %, _를 글자 그대로 찾도록 한다 (MySQL 기본 이스케이프 문자 \)
	static String escapeLike(String keyword) {
		return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private static BusinessHours businessHoursOf(Store store) {
		List<String> closedDays = store.getClosedDays().stream()
				.sorted()
				.map(DayOfWeekSetConverter::toCode)
				.toList();
		return new BusinessHours(store.getOpenTime(), store.getCloseTime(), store.getBreakStart(),
				store.getBreakEnd(), closedDays);
	}

	private static SignatureMenu toSignatureMenu(Menu menu) {
		return new SignatureMenu(menu.getId(), menu.getName(), menu.getDescription(), menu.getPrice(),
				menu.getImageUrl(), menu.isSoldOut());
	}

	private static BenefitSummary toBenefitSummary(Benefit benefit) {
		return new BenefitSummary(benefit.getId(), benefit.getTitle(), benefit.isPartner(), benefit.getPeriodText());
	}

}
