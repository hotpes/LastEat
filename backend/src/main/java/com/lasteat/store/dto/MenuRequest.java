package com.lasteat.store.dto;

import com.lasteat.store.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 사장님 메뉴 추가·수정 요청. 앞뒤 공백은 엔티티에서 지운다.
public record MenuRequest(
		@Schema(description = "메뉴 이름", example = "연어 아보카도 덮밥")
		@NotBlank(message = "메뉴 이름을 입력해 주세요.")
		@Size(max = Menu.MAX_NAME_LENGTH, message = "메뉴 이름은 30자 이하로 입력해 주세요.")
		String name,

		@Schema(description = "가격 (원)", example = "15000")
		@NotNull(message = "가격을 입력해 주세요.")
		@Min(value = 0, message = "가격은 0원 이상으로 입력해 주세요.")
		Integer price,

		@Schema(description = "메뉴 설명", example = "구운 연어와 고소한 아보카도의 조화")
		@Size(max = Menu.MAX_DESCRIPTION_LENGTH, message = "메뉴 설명은 100자 이하로 입력해 주세요.")
		String description,

		@Schema(description = "메뉴 사진 URL (업로드 API에서 받은 주소)", example = "https://cdn.lasteat.app/menus/21.jpg")
		@Size(max = Menu.MAX_IMAGE_URL_LENGTH, message = "사진 주소가 너무 깁니다.")
		String imageUrl,

		@Schema(description = "대표 메뉴 여부, 생략하면 false", example = "true")
		Boolean signature) {

	public boolean signatureOrDefault() {
		return Boolean.TRUE.equals(signature);
	}

}
