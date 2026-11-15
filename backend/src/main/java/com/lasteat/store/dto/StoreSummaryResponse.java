package com.lasteat.store.dto;

import com.lasteat.store.entity.BusinessStatus;
import com.lasteat.store.entity.StoreCategory;

// 목록 카드·지도 마커 한 개. distance는 기준 좌표가 없으면 null
public record StoreSummaryResponse(
		Long id,
		String name,
		StoreCategory category,
		String signatureMenuName,
		String thumbnailUrl,
		double lat,
		double lng,
		Integer distance,
		BusinessStatus businessStatus,
		boolean recommended,
		MainBenefit mainBenefit,
		boolean bookmarked) {

	public record MainBenefit(Long id, String title) {
	}

}
