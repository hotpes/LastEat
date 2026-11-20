package com.lasteat.store.dto;

import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

// 사장님 가게 정보 수정 요청. 사진·태그는 보낸 목록으로 통째로 바뀐다 (생략하면 비운다). 앞뒤 공백은 엔티티에서 지운다.
public record StoreUpdateRequest(
		@Schema(description = "가게 이름", example = "연남 온기")
		@NotBlank(message = "가게 이름을 입력해 주세요.")
		@Size(max = Store.MAX_NAME_LENGTH, message = "가게 이름은 30자 이하로 입력해 주세요.")
		String name,

		@Schema(description = "카테고리", example = "KOREAN")
		@NotNull(message = "카테고리를 선택해 주세요.")
		StoreCategory category,

		@Schema(description = "주소", example = "서울 마포구 동교로38길 12, 1층")
		@NotBlank(message = "주소를 입력해 주세요.")
		@Size(max = Store.MAX_ADDRESS_LENGTH, message = "주소가 너무 깁니다.")
		String address,

		@Schema(description = "위도. 주소를 바꾸면 앱이 지도 SDK로 구한 좌표", example = "37.5602")
		@NotNull(message = "위치를 지정해 주세요.")
		@DecimalMin(value = "-90", message = "위도 범위를 벗어났습니다.")
		@DecimalMax(value = "90", message = "위도 범위를 벗어났습니다.")
		Double lat,

		@Schema(description = "경도", example = "126.9246")
		@NotNull(message = "위치를 지정해 주세요.")
		@DecimalMin(value = "-180", message = "경도 범위를 벗어났습니다.")
		@DecimalMax(value = "180", message = "경도 범위를 벗어났습니다.")
		Double lng,

		@Schema(description = "가게 소개", example = "매일 아침 직접 끓이는 국물 요리")
		@Size(max = Store.MAX_DESCRIPTION_LENGTH, message = "가게 소개는 300자 이하로 입력해 주세요.")
		String description,

		@Schema(description = "전화번호 (숫자와 -)", example = "02-1234-5678")
		@Size(max = Store.MAX_PHONE_LENGTH, message = "전화번호가 너무 깁니다.")
		@Pattern(regexp = "^[0-9-]*$", message = "전화번호는 숫자와 -만 입력해 주세요.")
		String phone,

		@Schema(description = "사진 URL 목록, 첫 번째가 대표 사진 (업로드 API에서 받은 주소)",
				example = "[\"https://cdn.lasteat.app/stores/3/main.jpg\"]")
		@Size(max = Store.MAX_IMAGE_COUNT, message = "사진은 30장까지 등록할 수 있습니다.")
		List<@NotBlank(message = "사진 주소가 비어 있습니다.")
				@Size(max = StoreUpdateRequest.MAX_IMAGE_URL_LENGTH, message = "사진 주소가 너무 깁니다.") String> imageUrls,

		@Schema(description = "태그, 최대 5개·각 10자", example = "[\"혼밥 좋아요\", \"포장 가능\"]")
		@Size(max = Store.MAX_TAG_COUNT, message = "태그는 5개까지 등록할 수 있습니다.")
		List<@NotBlank(message = "빈 태그는 등록할 수 없습니다.")
				@Size(max = Store.MAX_TAG_LENGTH, message = "태그는 10자 이하로 입력해 주세요.") String> tags) {

	public static final int MAX_IMAGE_URL_LENGTH = 500;

	public List<String> imageUrlsOrEmpty() {
		return imageUrls == null ? List.of() : imageUrls;
	}

}
