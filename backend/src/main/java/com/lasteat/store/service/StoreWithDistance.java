package com.lasteat.store.service;

import com.lasteat.store.entity.Store;
import java.util.Objects;

// distance(m)는 기준 좌표가 없으면 null
public record StoreWithDistance(Store store, Integer distance) {

	public StoreWithDistance {
		Objects.requireNonNull(store, "가게가 필요합니다.");
	}

}
