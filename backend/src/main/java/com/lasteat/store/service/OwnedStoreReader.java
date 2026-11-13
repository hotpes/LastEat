package com.lasteat.store.service;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.entity.Member;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 사장님 API의 {storeId}가 로그인한 사장님 가게인지 확인한다. 모든 사장님 API가 먼저 거친다.
@Component
@RequiredArgsConstructor
public class OwnedStoreReader {

	private final StoreRepository storeRepository;

	// 없으면 STORE_NOT_FOUND, 사장님이 없거나 다른 사람 가게면 NOT_STORE_OWNER
	public Store read(Long storeId, Long memberId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
		// LAZY 프록시는 getId만으로 초기화되지 않는다
		Member owner = store.getOwner();
		if (owner == null || !owner.getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.NOT_STORE_OWNER);
		}
		return store;
	}

}
