package com.lasteat.bookmark.controller;

import com.lasteat.auth.jwt.AuthMember;
import com.lasteat.bookmark.dto.BookmarkSearchRequest;
import com.lasteat.bookmark.dto.BookmarkStatusResponse;
import com.lasteat.bookmark.dto.BookmarkedStoreResponse;
import com.lasteat.bookmark.service.BookmarkService;
import com.lasteat.global.config.OpenApiConfig;
import com.lasteat.global.response.ApiResponse;
import com.lasteat.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "저장")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@Operation(summary = "가게 저장", description = "이미 저장한 가게여도 성공. 에러: STORE_NOT_FOUND")
	@PostMapping("/stores/{storeId}/bookmark")
	public ApiResponse<BookmarkStatusResponse> add(@PathVariable Long storeId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(bookmarkService.add(member.memberId(), storeId));
	}

	@Operation(summary = "저장 취소", description = "저장하지 않은 가게여도 성공")
	@DeleteMapping("/stores/{storeId}/bookmark")
	public ApiResponse<BookmarkStatusResponse> remove(@PathVariable Long storeId,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(bookmarkService.remove(member.memberId(), storeId));
	}

	@Operation(summary = "내 저장 목록", description = "기본 최근 저장 순. sort=DISTANCE는 lat, lng 필요")
	@GetMapping("/members/me/bookmarks")
	public ApiResponse<PageResponse<BookmarkedStoreResponse>> list(
			@Valid @ParameterObject BookmarkSearchRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal AuthMember member) {
		return ApiResponse.ok(bookmarkService.list(member.memberId(), request));
	}

}
