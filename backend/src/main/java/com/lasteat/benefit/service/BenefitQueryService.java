package com.lasteat.benefit.service;

import com.lasteat.benefit.dto.BenefitResponse;
import com.lasteat.benefit.dto.BenefitSearchRequest;
import com.lasteat.benefit.entity.Benefit;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.benefit.repository.BenefitRepository.BenefitDistance;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.global.response.PageResponse;
import com.lasteat.store.repository.StoreImageRepository;
import com.lasteat.store.repository.StoreRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 손님용 할인 공지 조회. 공개 중인 공지만 보인다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BenefitQueryService {

	// 반경 안 후보 상한. 1차 범위(학교 주변 상권)에서는 넘지 않는다. 넘으면 먼 공지부터 빠진다.
	static final int MAX_CANDIDATES = 1000;

	private final BenefitRepository benefitRepository;

	private final StoreRepository storeRepository;

	private final StoreImageRepository storeImageRepository;

	public PageResponse<BenefitResponse> search(BenefitSearchRequest request) {
		GeoPoint center = new GeoPoint(request.lat(), request.lng());
		List<BenefitDistance> rows = benefitRepository.findNearbyPublished(center.toWkt(),
				center.boundingBoxWkt(request.radius()), request.radius(), request.partner(), MAX_CANDIDATES);
		List<BenefitDistance> pageRows = PageResponse.slice(rows, request.page(), request.size());

		Map<Long, Benefit> benefits = benefitRepository
				.findAllWithStoreByIdIn(pageRows.stream().map(BenefitDistance::getId).toList()).stream()
				.collect(Collectors.toMap(Benefit::getId, Function.identity()));
		Map<Long, String> thumbnails = storeImageRepository.findThumbnailUrls(
				benefits.values().stream().map(benefit -> benefit.getStore().getId()).distinct().toList());

		List<BenefitResponse> content = pageRows.stream()
				.map(row -> {
					Benefit benefit = benefits.get(row.getId());
					return BenefitResponse.of(benefit, thumbnails.get(benefit.getStore().getId()),
							(int) Math.round(row.getDistance()));
				})
				.toList();
		return PageResponse.of(content, request.page(), request.size(), rows.size());
	}

	public BenefitResponse getDetail(Long benefitId) {
		Benefit benefit = benefitRepository.findAllWithStoreByIdIn(List.of(benefitId)).stream()
				.filter(Benefit::isPublished)
				.findFirst()
				.orElseThrow(() -> new BusinessException(ErrorCode.BENEFIT_NOT_FOUND));
		Long storeId = benefit.getStore().getId();
		return BenefitResponse.of(benefit, storeImageRepository.findThumbnailUrls(List.of(storeId)).get(storeId),
				null);
	}

	public List<BenefitResponse> getStoreBenefits(Long storeId) {
		if (!storeRepository.existsById(storeId)) {
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}
		String thumbnail = storeImageRepository.findThumbnailUrls(List.of(storeId)).get(storeId);
		return benefitRepository.findPublishedByStoreIds(List.of(storeId)).stream()
				.map(benefit -> BenefitResponse.of(benefit, thumbnail, null))
				.toList();
	}

}
