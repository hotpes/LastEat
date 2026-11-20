package com.lasteat.registration.service;

import com.lasteat.auth.dto.TokenResponse;
import com.lasteat.auth.service.AuthService;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.member.entity.Member;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.registration.dto.OwnerRegistrationRequest;
import com.lasteat.registration.dto.OwnerRegistrationResponse;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 진위확인을 통과한 뒤 한 트랜잭션으로: 가게 생성(사업자번호 저장) → 사장님 전환 → 새 토큰 발급.
// 영업시간은 비워 두므로 사장님이 영업시간을 입력하기 전까지 가게는 CLOSED로 보인다.
@Component
@RequiredArgsConstructor
class OwnerRegistrationWriter {

	private final MemberRepository memberRepository;

	private final StoreRepository storeRepository;

	private final AuthService authService;

	@Transactional
	public OwnerRegistrationResponse createStoreAndPromote(Long memberId, String businessNumber,
			OwnerRegistrationRequest request) {
		Member member = memberRepository.findById(memberId)
				.filter(found -> !found.isWithdrawn())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
		Store store = newStore(member, businessNumber, request);
		try {
			// 같은 사업자번호로 동시에 등록하면 unique 제약에서 한쪽만 성공한다
			storeRepository.saveAndFlush(store);
		} catch (DataIntegrityViolationException e) {
			throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
		}
		member.promoteToOwner();
		TokenResponse tokens = authService.issueTokens(member);
		return OwnerRegistrationResponse.of(store.getId(), tokens);
	}

	private static Store newStore(Member member, String businessNumber, OwnerRegistrationRequest request) {
		var location = new GeoPoint(request.lat(), request.lng()).toPoint();
		Store store = Store.builder()
				.owner(member)
				.businessNumber(businessNumber)
				.name(request.storeName())
				.category(request.category())
				.address(request.address())
				.location(location)
				.build();
		try {
			// 공백 정리·길이 검증을 가게 정보 수정과 같은 규칙으로
			store.updateInfo(request.storeName(), request.category(), request.address(), location, request.phone(),
					null, List.of());
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		return store;
	}

}
