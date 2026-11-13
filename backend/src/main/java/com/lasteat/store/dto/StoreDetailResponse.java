package com.lasteat.store.dto;

import com.lasteat.store.entity.BusinessStatus;
import com.lasteat.store.entity.StoreCategory;
import java.time.LocalTime;
import java.util.List;

public record StoreDetailResponse(
		Long id,
		String name,
		StoreCategory category,
		String description,
		String address,
		String phone,
		double lat,
		double lng,
		Integer distance,
		List<String> imageUrls,
		List<String> tags,
		boolean recommended,
		BusinessStatus businessStatus,
		BusinessHours businessHours,
		List<SignatureMenu> signatureMenus,
		int menuCount,
		List<BenefitSummary> benefits,
		boolean bookmarked) {

	// closedDays는 요일 코드 (MON ~ SUN)
	public record BusinessHours(
			LocalTime openTime,
			LocalTime closeTime,
			LocalTime breakStart,
			LocalTime breakEnd,
			List<String> closedDays) {
	}

	public record SignatureMenu(Long id, String name, String description, int price, String imageUrl,
			boolean soldOut) {
	}

	// 공개 중인 할인 공지 요약. periodText는 사장님이 쓴 안내 기간
	public record BenefitSummary(Long id, String title, boolean partner, String periodText) {
	}

}
