package com.lasteat.store.dto;

import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

// 사장님 가게 정보 수정 요청. 이름·주소만 필수이고, 나머지는 보낸 칸만 바꾼다 (생략하거나 null이면 기존 값 유지).
// 설명·전화번호는 빈 문자열을 보내면 지운다. 앞뒤 공백은 엔티티에서 지운다.
// 사진은 imageUrls(목록 통째로 교체) 또는 mainImageUrl(대표 사진만 교체) 중 하나만 보낸다.
public record StoreUpdateRequest(
		@Schema(description = "가게 이름", example = "연남 온기")
		@NotBlank(message = "가게 이름을 입력해 주세요.")
		@Size(max = Store.MAX_NAME_LENGTH, message = "가게 이름은 30자 이하로 입력해 주세요.")
		String name,

		@Schema(description = "카테고리. 생략하면 그대로", example = "KOREAN")
		StoreCategory category,

		@Schema(description = "주소", example = "서울 마포구 동교로38길 12, 1층")
		@NotBlank(message = "주소를 입력해 주세요.")
		@Size(max = Store.MAX_ADDRESS_LENGTH, message = "주소가 너무 깁니다.")
		String address,

		@Schema(description = "위도. 경도와 함께 보내거나 둘 다 생략(좌표 그대로)", example = "37.5602")
		@DecimalMin(value = "-90", message = "위도 범위를 벗어났습니다.")
		@DecimalMax(value = "90", message = "위도 범위를 벗어났습니다.")
		Double lat,

		@Schema(description = "경도", example = "126.9246")
		@DecimalMin(value = "-180", message = "경도 범위를 벗어났습니다.")
		@DecimalMax(value = "180", message = "경도 범위를 벗어났습니다.")
		Double lng,

		@Schema(description = "가게 소개. 생략하면 그대로, 빈 문자열이면 지움", example = "매일 아침 직접 끓이는 국물 요리")
		@Size(max = Store.MAX_DESCRIPTION_LENGTH, message = "가게 소개는 300자 이하로 입력해 주세요.")
		String description,

		@Schema(description = "전화번호 (숫자와 -). 생략하면 그대로, 빈 문자열이면 지움", example = "02-1234-5678")
		@Size(max = Store.MAX_PHONE_LENGTH, message = "전화번호가 너무 깁니다.")
		@Pattern(regexp = "^[0-9-]*$", message = "전화번호는 숫자와 -만 입력해 주세요.")
		String phone,

		@Schema(description = "사진 URL 목록으로 통째로 교체, 첫 번째가 대표 사진. 생략하면 그대로",
				example = "[\"https://cdn.lasteat.app/stores/3/main.jpg\"]")
		@Size(max = Store.MAX_IMAGE_COUNT, message = "사진은 30장까지 등록할 수 있습니다.")
		List<@NotBlank(message = "사진 주소가 비어 있습니다.")
				@Size(max = StoreUpdateRequest.MAX_IMAGE_URL_LENGTH, message = "사진 주소가 너무 깁니다.") String> imageUrls,

		@Schema(description = "대표 사진(첫 번째)만 교체. 나머지 사진은 그대로. 사진이 없으면 대표 사진으로 추가",
				example = "https://cdn.lasteat.app/stores/3/new-main.jpg")
		@Size(max = StoreUpdateRequest.MAX_IMAGE_URL_LENGTH, message = "사진 주소가 너무 깁니다.")
		String mainImageUrl,

		@Schema(description = "태그 목록으로 통째로 교체, 최대 5개·각 10자. 생략하면 그대로", example = "[\"혼밥 좋아요\", \"포장 가능\"]")
		@Size(max = Store.MAX_TAG_COUNT, message = "태그는 5개까지 등록할 수 있습니다.")
		List<@NotBlank(message = "빈 태그는 등록할 수 없습니다.")
				@Size(max = Store.MAX_TAG_LENGTH, message = "태그는 10자 이하로 입력해 주세요.") String> tags) {

	public static final int MAX_IMAGE_URL_LENGTH = 500;

}
