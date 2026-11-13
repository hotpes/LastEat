package com.lasteat.closingdeal.service;

import com.lasteat.closingdeal.dto.ClosingDealCreateRequest;
import com.lasteat.closingdeal.dto.OwnerClosingDealResponse;
import com.lasteat.closingdeal.entity.ClosingDeal;
import com.lasteat.closingdeal.entity.ClosingDealStatus;
import com.lasteat.closingdeal.entity.ReservationStatus;
import com.lasteat.closingdeal.repository.ClosingDealRepository;
import com.lasteat.closingdeal.repository.ReservationRepository;
import com.lasteat.closingdeal.repository.ReservationRepository.QuantityByStatus;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.time.SeoulTime;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.MenuRepository;
import com.lasteat.store.service.OwnedStoreReader;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 사장님 마감할인 관리. 재고·상태를 바꾸는 작업은 DealWriteRetrier로 손님 예약과의 충돌을 다시 시도한다.
@Service
@RequiredArgsConstructor
public class OwnerClosingDealService {

	// 노출 시작부터 수령 마감까지 최대 시간
	static final Duration MAX_DURATION = Duration.ofHours(24);

	private final ClosingDealRepository closingDealRepository;

	private final ReservationRepository reservationRepository;

	private final MenuRepository menuRepository;

	private final OwnedStoreReader ownedStoreReader;

	private final DealWriteRetrier dealWriteRetrier;

	private final Clock clock;

	@Transactional
	public OwnerClosingDealResponse create(Long storeId, Long memberId, ClosingDealCreateRequest request) {
		Store store = ownedStoreReader.read(storeId, memberId);
		Menu menu = menuRepository.findById(request.menuId())
				.filter(found -> found.getStore().getId().equals(storeId))
				.orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDateTime openAt = SeoulTime.toLocal(request.openAt());
		LocalDateTime pickupEndAt = SeoulTime.toLocal(request.pickupEndAt());
		validate(request.salePrice(), menu.getPrice(), openAt, pickupEndAt, now);

		ClosingDeal deal = closingDealRepository.save(ClosingDeal.builder()
				.store(store)
				.menu(menu)
				.originalPrice(menu.getPrice())
				.salePrice(request.salePrice())
				.stock(request.stock())
				.openAt(openAt)
				.pickupEndAt(pickupEndAt)
				.build());
		return OwnerClosingDealResponse.of(deal, now, 0, 0);
	}

	@Transactional(readOnly = true)
	public List<OwnerClosingDealResponse> list(Long storeId, Long memberId, LocalDate date) {
		ownedStoreReader.read(storeId, memberId);
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate day = date == null ? now.toLocalDate() : date;
		List<ClosingDeal> deals = closingDealRepository.findAllByStoreIdAndOpenAtIn(storeId, day.atStartOfDay(),
				day.plusDays(1).atStartOfDay());
		return toResponses(deals, now);
	}

	public OwnerClosingDealResponse changeStock(Long storeId, Long dealId, Long memberId, int stock) {
		return modify(storeId, dealId, memberId, deal -> deal.changeStock(stock));
	}

	public OwnerClosingDealResponse close(Long storeId, Long dealId, Long memberId) {
		return modify(storeId, dealId, memberId, ClosingDeal::close);
	}

	// 마감된(조기 마감·수령 마감 지남) 마감할인은 바꿀 수 없다
	private OwnerClosingDealResponse modify(Long storeId, Long dealId, Long memberId, Consumer<ClosingDeal> change) {
		return dealWriteRetrier.run(() -> {
			ownedStoreReader.read(storeId, memberId);
			ClosingDeal deal = closingDealRepository.findByIdAndStoreId(dealId, storeId)
					.orElseThrow(() -> new BusinessException(ErrorCode.CLOSING_DEAL_NOT_FOUND));
			LocalDateTime now = LocalDateTime.now(clock);
			if (deal.statusAt(now) == ClosingDealStatus.CLOSED) {
				throw new BusinessException(ErrorCode.CLOSING_DEAL_NOT_OPEN);
			}
			change.accept(deal);
			return toResponses(List.of(deal), now).getFirst();
		});
	}

	private List<OwnerClosingDealResponse> toResponses(List<ClosingDeal> deals, LocalDateTime now) {
		if (deals.isEmpty()) {
			return List.of();
		}
		Map<Long, Map<ReservationStatus, Long>> quantities = reservationRepository
				.sumQuantities(deals.stream().map(ClosingDeal::getId).toList()).stream()
				.collect(Collectors.groupingBy(QuantityByStatus::getDealId,
						Collectors.toMap(QuantityByStatus::getStatus, QuantityByStatus::getQuantity)));
		return deals.stream()
				.map(deal -> {
					Map<ReservationStatus, Long> byStatus = quantities.getOrDefault(deal.getId(), Map.of());
					return OwnerClosingDealResponse.of(deal, now,
							byStatus.getOrDefault(ReservationStatus.RESERVED, 0L),
							byStatus.getOrDefault(ReservationStatus.PICKED_UP, 0L));
				})
				.toList();
	}

	private static void validate(int salePrice, int menuPrice, LocalDateTime openAt, LocalDateTime pickupEndAt,
			LocalDateTime now) {
		if (salePrice >= menuPrice) {
			throw BusinessException.invalidInput("salePrice",
					"할인가는 메뉴 가격(" + menuPrice + "원)보다 낮아야 합니다.");
		}
		if (!pickupEndAt.isAfter(openAt)) {
			throw BusinessException.invalidInput("pickupEndAt", "수령 마감은 노출 시작보다 늦어야 합니다.");
		}
		if (!pickupEndAt.isAfter(now)) {
			throw BusinessException.invalidInput("pickupEndAt", "수령 마감은 지금 이후여야 합니다.");
		}
		if (pickupEndAt.isAfter(openAt.plus(MAX_DURATION))) {
			throw BusinessException.invalidInput("pickupEndAt", "수령 마감은 노출 시작부터 24시간 이내여야 합니다.");
		}
	}

}
