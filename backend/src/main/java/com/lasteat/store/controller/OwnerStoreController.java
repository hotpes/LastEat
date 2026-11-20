package com.lasteat.store.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.store.dto.BusinessStatusChangeRequest;
import com.lasteat.store.dto.BusinessStatusResponse;
import com.lasteat.store.dto.MenuResponse;
import com.lasteat.store.dto.OwnerStoreDetailResponse;
import com.lasteat.store.dto.OwnerStoreSummaryResponse;
import com.lasteat.store.dto.StoreHoursRequest;
import com.lasteat.store.dto.StoreUpdateRequest;
import com.lasteat.store.service.OwnerStoreQueryService;
import com.lasteat.store.service.OwnerStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	private final OwnerStoreService ownerStoreService;

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

	@Operation(summary = "가게 정보 수정", description = "사진·태그는 보낸 목록으로 통째로 바뀐다 (생략하면 비운다). "
			+ "주소를 바꾸면 앱이 지도 SDK로 구한 좌표를 함께 보낸다. 영업시간·영업 상태는 바뀌지 않는다. "
			+ "응답은 가게 정보 조회와 같은 형식. 에러: STORE_NOT_FOUND, NOT_STORE_OWNER, INVALID_INPUT")
	@PutMapping("/{storeId}")
	public ApiResponse<OwnerStoreDetailResponse> update(@PathVariable Long storeId,
			@Valid @RequestBody StoreUpdateRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		ownerStoreService.update(storeId, member.memberId(), request);
		return ApiResponse.ok(ownerStoreQueryService.getMyStore(storeId, member.memberId()));
	}

	@Operation(summary = "영업시간 수정", description = "쉬는 시간은 둘 다 보내거나 둘 다 생략, 시작이 끝보다 빨라야 한다. "
			+ "응답은 가게 정보 조회와 같은 형식. 에러: STORE_NOT_FOUND, NOT_STORE_OWNER, INVALID_INPUT")
	@PutMapping("/{storeId}/hours")
	public ApiResponse<OwnerStoreDetailResponse> changeHours(@PathVariable Long storeId,
			@Valid @RequestBody StoreHoursRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		ownerStoreService.changeHours(storeId, member.memberId(), request);
		return ApiResponse.ok(ownerStoreQueryService.getMyStore(storeId, member.memberId()));
	}

	@Operation(summary = "오늘 영업 상태 변경", description = "OPEN·BREAK·CLOSED는 오늘 하루만 적용, AUTO는 영업시간 기준. "
			+ "에러: STORE_NOT_FOUND, NOT_STORE_OWNER, INVALID_INPUT")
	@PatchMapping("/{storeId}/business-status")
	public ApiResponse<BusinessStatusResponse> changeBusinessStatus(@PathVariable Long storeId,
			@Valid @RequestBody BusinessStatusChangeRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerStoreService.changeBusinessStatus(storeId, member.memberId(), request.status()));
	}

	@Operation(summary = "메뉴 목록 (사장님)", description = "대표 메뉴 먼저, 그다음 등록 순. "
			+ "에러: STORE_NOT_FOUND, NOT_STORE_OWNER")
	@GetMapping("/{storeId}/menus")
	public ApiResponse<List<MenuResponse>> getMenus(@PathVariable Long storeId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(ownerStoreQueryService.getMenus(storeId, member.memberId()));
	}

}
