package com.lasteat.benefit.service;

import com.lasteat.benefit.dto.BenefitRequest;
import com.lasteat.benefit.dto.OwnerBenefitResponse;
import com.lasteat.benefit.entity.Benefit;
import com.lasteat.benefit.repository.BenefitRepository;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.store.entity.Store;
import com.lasteat.store.service.OwnedStoreReader;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 사장님 할인 공지 관리. 모든 메서드가 먼저 본인 가게인지 확인한다.
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerBenefitService {

	private final BenefitRepository benefitRepository;

	private final OwnedStoreReader ownedStoreReader;

	private final Clock clock;

	@Transactional(readOnly = true)
	public List<OwnerBenefitResponse> getBenefits(Long storeId, Long memberId) {
		ownedStoreReader.read(storeId, memberId);
		return benefitRepository.findAllByStoreIdLatestFirst(storeId).stream()
				.map(OwnerBenefitResponse::from)
				.toList();
	}

	public OwnerBenefitResponse create(Long storeId, Long memberId, BenefitRequest request) {
		Store store = ownedStoreReader.read(storeId, memberId);
		Benefit benefit = asInvalidInput(() -> Benefit.builder()
				.store(store)
				.title(request.title())
				.content(request.content())
				.periodText(request.periodText())
				.publishedAt(LocalDateTime.now(clock))
				.build());
		return OwnerBenefitResponse.from(benefitRepository.save(benefit));
	}

	public OwnerBenefitResponse update(Long storeId, Long benefitId, Long memberId, BenefitRequest request) {
		Benefit benefit = findOwned(storeId, benefitId, memberId);
		asInvalidInput(() -> {
			benefit.update(request.title(), request.content(), request.periodText());
			return benefit;
		});
		return OwnerBenefitResponse.from(benefit);
	}

	public OwnerBenefitResponse changePublished(Long storeId, Long benefitId, Long memberId, boolean published) {
		Benefit benefit = findOwned(storeId, benefitId, memberId);
		benefit.changePublished(published);
		return OwnerBenefitResponse.from(benefit);
	}

	public void delete(Long storeId, Long benefitId, Long memberId) {
		benefitRepository.delete(findOwned(storeId, benefitId, memberId));
	}

	// 요청 검증(@NotBlank)을 통과해도 엔티티가 막는 값(유니코드 공백만 있는 글 등)은 500이 아니라 입력값 오류로
	private static Benefit asInvalidInput(Supplier<Benefit> action) {
		try {
			return action.get();
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private Benefit findOwned(Long storeId, Long benefitId, Long memberId) {
		ownedStoreReader.read(storeId, memberId);
		return benefitRepository.findByIdAndStoreId(benefitId, storeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.BENEFIT_NOT_FOUND));
	}

}
