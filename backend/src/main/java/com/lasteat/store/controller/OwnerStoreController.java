package com.lasteat.store.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.store.dto.MenuResponse;
import com.lasteat.store.dto.OwnerStoreDetailResponse;
import com.lasteat.store.dto.OwnerStoreSummaryResponse;
import com.lasteat.store.service.OwnerStoreQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 사장님 계정만 (SecurityConfig). 본인 가게가 아니면 NOT_STORE_OWNER
@Tag(name = "사장님")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1/owner/stores")
@RequiredArgsConstructor
public class OwnerStoreController {

	private final OwnerStoreQueryService ownerStoreQueryService;

	@Operation(summary = "내 가게 목록", description = "등록 순. 가게가 없으면 빈 배열")
	@GetMapping
	public ApiResponse<List<OwnerStoreSummaryResponse>> getMyStores(
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerStoreQueryService.getMyStores(member.memberId()));
	}

	@Operation(summary = "가게 정보 조회", description = "가게 상세 형식 + businessStatusOverride. "
			+ "에러: STORE_NOT_FOUND, NOT_STORE_OWNER")
	@GetMapping("/{storeId}")
	public ApiResponse<OwnerStoreDetailResponse> getMyStore(@PathVariable Long storeId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerStoreQueryService.getMyStore(storeId, member.memberId()));
	}

	@Operation(summary = "메뉴 목록 (사장님)", description = "대표 메뉴 먼저, 그다음 등록 순. "
			+ "에러: STORE_NOT_FOUND, NOT_STORE_OWNER")
	@GetMapping("/{storeId}/menus")
	public ApiResponse<List<MenuResponse>> getMenus(@PathVariable Long storeId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerStoreQueryService.getMenus(storeId, member.memberId()));
	}

}
