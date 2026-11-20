package com.lasteat.registration.dto;

import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// 사장님 등록 요청. 사업자 정보 3개(번호·개업일자·대표자)는 사업자등록증 OCR 값으로 국세청 진위확인에 쓰고,
// 나머지는 새 가게 정보다. 가게 이름은 상호와 달라도 된다 (국세청에 보내지 않음).
public record OwnerRegistrationRequest(
		@Schema(description = "사업자등록번호 10자리, - 포함 가능", example = "123-45-67890")
		@NotBlank(message = "사업자등록번호를 입력해 주세요.")
		@Pattern(regexp = "^\\s*\\d{3}-?\\d{2}-?\\d{5}\\s*$", message = "사업자등록번호는 숫자 10자리로 입력해 주세요.")
		String businessNumber,

		@Schema(description = "개업연월일 (사업자등록증의 개업연월일)", example = "2021-03-15", type = "string")
		@NotNull(message = "개업연월일을 입력해 주세요.")
		@PastOrPresent(message = "개업연월일이 미래일 수 없습니다.")
		LocalDate openDate,

		@Schema(description = "대표자 성명", example = "홍길동")
		@NotBlank(message = "대표자 성명을 입력해 주세요.")
		@Size(max = 30, message = "대표자 성명이 너무 깁니다.")
		String representativeName,

		@Schema(description = "가게 이름 (손님에게 보이는 이름)", example = "연남 온기")
		@NotBlank(message = "가게 이름을 입력해 주세요.")
		@Size(max = Store.MAX_NAME_LENGTH, message = "가게 이름은 30자 이하로 입력해 주세요.")
		String storeName,

		@Schema(description = "카테고리", example = "KOREAN")
		@NotNull(message = "카테고리를 선택해 주세요.")
		StoreCategory category,

		@Schema(description = "가게 주소", example = "서울 동작구 상도로 1")
		@NotBlank(message = "주소를 입력해 주세요.")
		@Size(max = Store.MAX_ADDRESS_LENGTH, message = "주소가 너무 깁니다.")
		String address,

		@Schema(description = "위도 (앱이 지도 SDK로 구한 좌표)", example = "37.4972")
		@NotNull(message = "위치를 지정해 주세요.")
		@DecimalMin(value = "-90", message = "위도 범위를 벗어났습니다.")
		@DecimalMax(value = "90", message = "위도 범위를 벗어났습니다.")
		Double lat,

		@Schema(description = "경도", example = "126.9575")
		@NotNull(message = "위치를 지정해 주세요.")
		@DecimalMin(value = "-180", message = "경도 범위를 벗어났습니다.")
		@DecimalMax(value = "180", message = "경도 범위를 벗어났습니다.")
		Double lng,

		@Schema(description = "가게 전화번호 (숫자와 -)", example = "02-1234-5678")
		@Size(max = Store.MAX_PHONE_LENGTH, message = "전화번호가 너무 깁니다.")
		@Pattern(regexp = "^[0-9-]*$", message = "전화번호는 숫자와 -만 입력해 주세요.")
		String phone) {

	// 하이픈·공백을 뺀 숫자 10자리
	public String businessNumberDigits() {
		return businessNumber.replaceAll("[^0-9]", "");
	}

}
