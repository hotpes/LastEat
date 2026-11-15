package com.lasteat.store.entity;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum StoreCategory {
	KOREAN("한식"),
	WESTERN("양식"),
	JAPANESE("일식"),
	CHINESE("중식"),
	CAFE("카페"),
	BAKERY("베이커리"),
	ETC("기타");

	// 화면에 보이는 이름. 키워드 검색("카페")에서도 쓴다
	private final String displayName;

	StoreCategory(String displayName) {
		this.displayName = displayName;
	}

	public static List<StoreCategory> matchingKeyword(String keyword) {
		return Arrays.stream(values())
				.filter(category -> category.displayName.contains(keyword))
				.toList();
	}

}
