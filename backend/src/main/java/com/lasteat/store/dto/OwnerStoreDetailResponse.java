package com.lasteat.store.dto;

import com.lasteat.store.dto.StoreDetailResponse.BenefitSummary;
import com.lasteat.store.dto.StoreDetailResponse.BusinessHours;
import com.lasteat.store.dto.StoreDetailResponse.SignatureMenu;
import com.lasteat.store.entity.BusinessStatus;
import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

// 사장님용 가게 정보. 손님용 가게 상세에서 거리·저장 여부를 빼고, 오늘 직접 정한 영업 상태를 더했다.
// StoreDetailResponse에 필드를 추가하면 여기에도 같이 추가한다.
public record OwnerStoreDetailResponse(
		Long id,
		String name,
		StoreCategory category,
		String description,
		String address,
		String phone,
		double lat,
		double lng,
		List<String> imageUrls,
		List<String> tags,
		boolean recommended,
		BusinessStatus businessStatus,
		@Schema(description = "사장님이 오늘 직접 정한 영업 상태. 정하지 않았거나 지난 날짜면 null", example = "CLOSED")
		BusinessStatus businessStatusOverride,
		BusinessHours businessHours,
		List<SignatureMenu> signatureMenus,
		int menuCount,
		List<BenefitSummary> benefits) {

	public static OwnerStoreDetailResponse of(StoreDetailResponse detail, BusinessStatus businessStatusOverride) {
		return new OwnerStoreDetailResponse(
				detail.id(),
				detail.name(),
				detail.category(),
				detail.description(),
				detail.address(),
				detail.phone(),
				detail.lat(),
				detail.lng(),
				detail.imageUrls(),
				detail.tags(),
				detail.recommended(),
				detail.businessStatus(),
				businessStatusOverride,
				detail.businessHours(),
				detail.signatureMenus(),
				detail.menuCount(),
				detail.benefits());
	}

}
