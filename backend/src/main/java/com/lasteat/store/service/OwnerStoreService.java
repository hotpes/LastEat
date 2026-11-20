package com.lasteat.store.service;

import com.lasteat.global.converter.DayOfWeekSetConverter;
import com.lasteat.global.converter.StringListConverter;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.store.dto.BusinessStatusChangeRequest;
import com.lasteat.store.dto.BusinessStatusResponse;
import com.lasteat.store.dto.StoreHoursRequest;
import com.lasteat.store.dto.StoreUpdateRequest;
import com.lasteat.store.entity.BusinessStatus;
import com.lasteat.store.entity.Store;
import com.lasteat.store.entity.StoreImage;
import com.lasteat.store.repository.StoreImageRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 사장님 가게 정보·영업시간·오늘 영업 상태 변경. 모든 메서드가 먼저 본인 가게인지 확인한다.
// 수정 결과 응답(가게 정보 조회 형식)은 컨트롤러가 커밋 뒤 OwnerStoreQueryService로 다시 읽는다.
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerStoreService {

	private final OwnedStoreReader ownedStoreReader;

	private final StoreImageRepository storeImageRepository;

	private final Clock clock;

	public void update(Long storeId, Long memberId, StoreUpdateRequest request) {
		Store store = ownedStoreReader.read(storeId, memberId);
		// 태그는 DB에 쉼표로 이어 저장한다
		if (request.tags() != null
				&& request.tags().stream().anyMatch(tag -> tag.contains(StringListConverter.DELIMITER))) {
			throw BusinessException.invalidInput("tags", "태그에는 쉼표를 쓸 수 없습니다.");
		}
		try {
			store.updateInfo(request.name(), request.category(), request.address(),
					new GeoPoint(request.lat(), request.lng()).toPoint(), request.phone(), request.description(),
					request.tags());
		} catch (IllegalArgumentException e) {
			// DTO 검증을 통과해도 엔티티가 막는 값(유니코드 공백만 있는 이름 등)은 500이 아니라 입력값 오류로
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		replaceImages(store, request.imageUrlsOrEmpty());
	}

	public void changeHours(Long storeId, Long memberId, StoreHoursRequest request) {
		Store store = ownedStoreReader.read(storeId, memberId);
		if ((request.breakStart() == null) != (request.breakEnd() == null)) {
			throw BusinessException.invalidInput("breakStart", "쉬는 시간은 시작과 끝을 함께 입력해 주세요.");
		}
		if (request.breakStart() != null && !request.breakStart().isBefore(request.breakEnd())) {
			throw BusinessException.invalidInput("breakEnd", "쉬는 시간 끝은 시작보다 늦어야 합니다.");
		}
		store.changeHours(request.openTime(), request.closeTime(), request.breakStart(), request.breakEnd(),
				toDays(request.closedDays()));
	}

	public BusinessStatusResponse changeBusinessStatus(Long storeId, Long memberId,
			BusinessStatusChangeRequest.Status status) {
		Store store = ownedStoreReader.read(storeId, memberId);
		LocalDateTime now = LocalDateTime.now(clock);
		status.toOverride().ifPresentOrElse(
				override -> store.overrideBusinessStatus(override, now.toLocalDate()),
				store::clearBusinessStatusOverride);
		BusinessStatus override = store.businessStatusOverrideOn(now.toLocalDate()).orElse(null);
		return new BusinessStatusResponse(store.businessStatusAt(now), override);
	}

	// 보낸 순서대로 다시 저장한다. 첫 번째(sortOrder 0)가 대표 사진
	private void replaceImages(Store store, List<String> imageUrls) {
		storeImageRepository.deleteAllByStoreId(store.getId());
		List<StoreImage> images = IntStream.range(0, imageUrls.size())
				.mapToObj(order -> StoreImage.builder()
						.store(store).imageUrl(imageUrls.get(order).strip()).sortOrder(order).build())
				.toList();
		storeImageRepository.saveAll(images);
	}

	private static Set<DayOfWeek> toDays(List<String> codes) {
		if (codes == null || codes.isEmpty()) {
			return Set.of();
		}
		List<DayOfWeek> days = new ArrayList<>();
		for (String code : codes) {
			try {
				days.add(DayOfWeekSetConverter.fromCode(code));
			} catch (IllegalArgumentException e) {
				throw BusinessException.invalidInput("closedDays", "요일은 MON~SUN 코드로 보내 주세요.");
			}
		}
		return EnumSet.copyOf(days);
	}

}
