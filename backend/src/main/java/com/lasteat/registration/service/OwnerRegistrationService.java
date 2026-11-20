package com.lasteat.registration.service;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.registration.dto.OwnerRegistrationRequest;
import com.lasteat.registration.dto.OwnerRegistrationResponse;
import com.lasteat.registration.nts.BusinessInfo;
import com.lasteat.registration.nts.NtsClient;
import com.lasteat.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 사장님 등록: 사업자번호 중복 확인 → 국세청 진위확인 → 가게 생성·사장님 전환·새 토큰 (OwnerRegistrationWriter).
// 국세청 호출(최대 수 초) 동안 DB 트랜잭션을 잡고 있지 않도록 여기는 트랜잭션 없이 순서만 정한다.
@Service
@RequiredArgsConstructor
public class OwnerRegistrationService {

	private final MemberRepository memberRepository;

	private final StoreRepository storeRepository;

	private final NtsClient ntsClient;

	private final OwnerRegistrationWriter writer;

	public OwnerRegistrationResponse register(Long memberId, OwnerRegistrationRequest request) {
		// 국세청 호출 횟수를 아끼려고 먼저 막을 수 있는 것부터 확인한다. 동시 요청은 writer의 unique 제약이 막는다
		memberRepository.findById(memberId)
				.filter(member -> !member.isWithdrawn())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
		String businessNumber = request.businessNumberDigits();
		if (storeRepository.existsByBusinessNumber(businessNumber)) {
			throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
		}
		var verification = ntsClient.verify(new BusinessInfo(businessNumber, request.openDate(),
				request.representativeName().strip()));
		switch (verification) {
			case NOT_MATCHED -> throw new BusinessException(ErrorCode.BUSINESS_NOT_VERIFIED);
			case NOT_ACTIVE -> throw new BusinessException(ErrorCode.BUSINESS_NOT_ACTIVE);
			case ACTIVE -> {
				// 통과
			}
		}
		return writer.createStoreAndPromote(memberId, businessNumber, request);
	}

}
