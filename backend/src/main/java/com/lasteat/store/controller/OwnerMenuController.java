package com.lasteat.store.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.store.dto.MenuRequest;
import com.lasteat.store.dto.MenuResponse;
import com.lasteat.store.dto.MenuSoldOutRequest;
import com.lasteat.store.service.OwnerMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 사장님 메뉴 관리. 목록 조회는 OwnerStoreController. 모든 API 에러: STORE_NOT_FOUND, NOT_STORE_OWNER
@Tag(name = "사장님")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1/owner/stores/{storeId}/menus")
@RequiredArgsConstructor
public class OwnerMenuController {

	private final OwnerMenuService ownerMenuService;

	@Operation(summary = "메뉴 추가", description = "에러: INVALID_INPUT")
	@PostMapping
	public ApiResponse<MenuResponse> create(@PathVariable Long storeId, @Valid @RequestBody MenuRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerMenuService.create(storeId, member.memberId(), request));
	}

	@Operation(summary = "메뉴 수정", description = "품절 여부는 그대로. 에러: MENU_NOT_FOUND, INVALID_INPUT")
	@PutMapping("/{menuId}")
	public ApiResponse<MenuResponse> update(@PathVariable Long storeId, @PathVariable Long menuId,
			@Valid @RequestBody MenuRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerMenuService.update(storeId, menuId, member.memberId(), request));
	}

	@Operation(summary = "메뉴 품절 표시", description = "에러: MENU_NOT_FOUND, INVALID_INPUT")
	@PatchMapping("/{menuId}/sold-out")
	public ApiResponse<MenuResponse> changeSoldOut(@PathVariable Long storeId, @PathVariable Long menuId,
			@Valid @RequestBody MenuSoldOutRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerMenuService.changeSoldOut(storeId, menuId, member.memberId(), request.soldOut()));
	}

	@Operation(summary = "메뉴 삭제", description = "손님·사장님 메뉴 목록에서 빠진다. 지난 마감할인·예약 기록은 남는다. "
			+ "진행 중인 마감할인은 그대로라 필요하면 조기 마감한다. 응답 data는 null. 에러: MENU_NOT_FOUND")
	@DeleteMapping("/{menuId}")
	public ApiResponse<Void> delete(@PathVariable Long storeId, @PathVariable Long menuId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		ownerMenuService.delete(storeId, menuId, member.memberId());
		return ApiResponse.ok();
	}

}
