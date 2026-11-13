package com.lasteat.closingdeal.service;

import com.lasteat.closingdeal.dto.ClosingDealResponse;
import com.lasteat.closingdeal.dto.ClosingDealSearchRequest;
import com.lasteat.closingdeal.dto.ClosingDealSort;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.repository.ClosingDealRepository;
import com.lasteat.closingdeal.repository.ClosingDealRepository.DealDistance;
import com.lasteat.closingdeal.repository.ReservationRepository;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.global.response.PageResponse;
import com.lasteat.store.entity.StoreCategory;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
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

// 손님용 마감할인 조회. 노출 중(예약 가능·품절)인 것만 보인다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClosingDealQueryService {

	// 반경 안 후보 상한. 1차 범위(학교 주변 상권)에서는 넘지 않는다.
	static final int MAX_CANDIDATES = 1000;

	private final ClosingDealRepository closingDealRepository;

	private final ReservationRepository reservationRepository;

	private final StoreRepository storeRepository;

	private final StoreImageRepository storeImageRepository;

	private final Clock clock;

	public PageResponse<ClosingDealResponse> search(ClosingDealSearchRequest request) {
		LocalDateTime now = LocalDateTime.now(clock);
		GeoPoint center = new GeoPoint(request.lat(), request.lng());
		List<StoreCategory> categories = request.category().isEmpty()
				? Arrays.asList(StoreCategory.values())
				: request.category();
		List<DealDistance> rows = closingDealRepository.findNearbyVisible(center.toWkt(),
				center.boundingBoxWkt(request.radius()), request.radius(),
				categories.stream().map(Enum::name).toList(), request.available(),
				request.sort() == ClosingDealSort.ENDING_SOON, now, MAX_CANDIDATES);
		List<DealDistance> pageRows = PageResponse.slice(rows, request.page(), request.size());

		Map<Long, ClosingDeal> deals = closingDealRepository
				.findAllWithMenuAndStoreByIdIn(pageRows.stream().map(DealDistance::getId).toList()).stream()
				.collect(Collectors.toMap(ClosingDeal::getId, Function.identity()));
		Map<Long, String> thumbnails = storeImageRepository.findThumbnailUrls(
				deals.values().stream().map(deal -> deal.getStore().getId()).distinct().toList());
		List<ClosingDealResponse> content = pageRows.stream()
				.map(row -> {
					ClosingDeal deal = deals.get(row.getId());
					return ClosingDealResponse.of(deal, now, thumbnails.get(deal.getStore().getId()),
							(int) Math.round(row.getDistance()), null);
				})
				.toList();
		return PageResponse.of(content, request.page(), request.size(), rows.size());
	}

	public List<ClosingDealResponse> getStoreDeals(Long storeId) {
		if (!storeRepository.existsById(storeId)) {
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}
		LocalDateTime now = LocalDateTime.now(clock);
		String thumbnail = storeImageRepository.findThumbnailUrls(List.of(storeId)).get(storeId);
		return closingDealRepository.findVisibleByStoreId(storeId, now).stream()
				.map(deal -> ClosingDealResponse.of(deal, now, thumbnail, null, null))
				.toList();
	}

	// memberId가 null이면 myReservationId도 null
	public ClosingDealResponse getDetail(Long dealId, Optional<GeoPoint> origin, Long memberId) {
		LocalDateTime now = LocalDateTime.now(clock);
		ClosingDeal deal = closingDealRepository.findWithMenuAndStore(dealId)
				.filter(found -> found.isVisibleAt(now))
				.orElseThrow(() -> new BusinessException(ErrorCode.CLOSING_DEAL_NOT_FOUND));
		Long storeId = deal.getStore().getId();
		Integer distance = origin.map(point -> point.distanceMetersTo(GeoPoint.from(deal.getStore().getLocation())))
				.orElse(null);
		Long myReservationId = memberId == null
				? null
				: reservationRepository.findReservedIds(memberId, dealId).stream().findFirst().orElse(null);
		return ClosingDealResponse.of(deal, now, storeImageRepository.findThumbnailUrls(List.of(storeId)).get(storeId),
				distance, myReservationId);
	}

}
