package com.lasteat.store.service;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.store.dto.MenuRequest;
import com.lasteat.store.dto.MenuResponse;
import com.lasteat.store.entity.Menu;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.MenuRepository;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 사장님 메뉴 추가·수정·삭제·품절. 모든 메서드가 먼저 본인 가게인지 확인한다.
// 삭제는 표시만 한다 (지난 마감할인·예약이 메뉴를 참조). 진행 중인 마감할인은 그대로 두고, 필요하면 사장님이 조기 마감한다.
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerMenuService {

	private final MenuRepository menuRepository;

	private final OwnedStoreReader ownedStoreReader;

	public MenuResponse create(Long storeId, Long memberId, MenuRequest request) {
		Store store = ownedStoreReader.read(storeId, memberId);
		Menu menu = asInvalidInput(() -> Menu.builder()
				.store(store)
				.name(request.name())
				.description(request.description())
				.price(request.price())
				.imageUrl(request.imageUrl())
				.signature(request.signatureOr(false))
				.build());
		return MenuResponse.from(menuRepository.save(menu));
	}

	public MenuResponse update(Long storeId, Long menuId, Long memberId, MenuRequest request) {
		Menu menu = findOwned(storeId, menuId, memberId);
		asInvalidInput(() -> {
			menu.update(request.name(), request.description(), request.price(), request.imageUrl(),
					request.signatureOr(menu.isSignature()));
			return menu;
		});
		return MenuResponse.from(menu);
	}

	public MenuResponse changeSoldOut(Long storeId, Long menuId, Long memberId, boolean soldOut) {
		Menu menu = findOwned(storeId, menuId, memberId);
		menu.changeSoldOut(soldOut);
		return MenuResponse.from(menu);
	}

	public void delete(Long storeId, Long menuId, Long memberId) {
		findOwned(storeId, menuId, memberId).delete();
	}

	// 요청 검증을 통과해도 엔티티가 막는 값(유니코드 공백만 있는 이름 등)은 500이 아니라 입력값 오류로
	private static Menu asInvalidInput(Supplier<Menu> action) {
		try {
			return action.get();
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private Menu findOwned(Long storeId, Long menuId, Long memberId) {
		ownedStoreReader.read(storeId, memberId);
		return menuRepository.findActiveByIdAndStoreId(menuId, storeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
	}

}
