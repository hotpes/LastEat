package com.lasteat.bookmark.service;

import com.lasteat.BackendApplication;
import com.lasteat.bookmark.dto.BookmarkSearchRequest;
import com.lasteat.bookmark.dto.BookmarkSort;
import com.lasteat.bookmark.dto.BookmarkStatusResponse;
import com.lasteat.bookmark.dto.BookmarkedStoreResponse;
import com.lasteat.bookmark.entity.Bookmark;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.global.geo.GeoPoint;
import com.lasteat.global.response.PageResponse;
import com.lasteat.store.dto.StoreSummaryResponse;
import com.lasteat.store.entity.BusinessStatus;
import com.lasteat.store.entity.Store;
import com.lasteat.store.repository.StoreRepository;
import com.lasteat.store.service.StoreSummaryAssembler;
import com.lasteat.store.service.StoreWithDistance;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

	private static final ZoneId ZONE = ZoneId.of(BackendApplication.TIME_ZONE);

	private final BookmarkRepository bookmarkRepository;

	private final StoreRepository storeRepository;

	private final StoreSummaryAssembler storeSummaryAssembler;

	private final Clock clock;

	// 이미 저장했어도 성공
	public BookmarkStatusResponse add(Long memberId, Long storeId) {
		if (!storeRepository.existsById(storeId)) {
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}
		bookmarkRepository.insertIfAbsent(memberId, storeId, LocalDateTime.now(clock));
		return new BookmarkStatusResponse(true);
	}

	// 저장하지 않은 가게여도 성공
	public BookmarkStatusResponse remove(Long memberId, Long storeId) {
		bookmarkRepository.deleteByMemberIdAndStoreId(memberId, storeId);
		return new BookmarkStatusResponse(false);
	}

	// 한 사람의 저장 목록은 많지 않아서 전부 읽은 뒤 거르고 자른다
	@Transactional(readOnly = true)
	public PageResponse<BookmarkedStoreResponse> list(Long memberId, BookmarkSearchRequest request) {
		Optional<GeoPoint> origin = GeoPoint.ofNullable(request.lat(), request.lng());
		if (request.sort() == BookmarkSort.DISTANCE && origin.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		LocalDateTime now = LocalDateTime.now(clock);
		// 조회 결과는 이미 최근 저장 순
		List<Entry> matched = bookmarkRepository.findAllWithStoreByMemberId(memberId).stream()
				.filter(bookmark -> matches(bookmark.getStore(), request, now))
				.map(bookmark -> Entry.of(bookmark, origin))
				.toList();
		if (request.sort() == BookmarkSort.DISTANCE) {
			matched = matched.stream().sorted(Entry.BY_DISTANCE).toList();
		}
		List<Entry> pageItems = PageResponse.slice(matched, request.page(), request.size());
		List<StoreSummaryResponse> summaries = storeSummaryAssembler.assemble(
				pageItems.stream().map(Entry::item).toList(), memberId, now);
		List<BookmarkedStoreResponse> content = IntStream.range(0, pageItems.size())
				.mapToObj(i -> new BookmarkedStoreResponse(summaries.get(i), pageItems.get(i).bookmarkedAt()))
				.toList();
		return PageResponse.of(content, request.page(), request.size(), matched.size());
	}

	private static boolean matches(Store store, BookmarkSearchRequest request, LocalDateTime now) {
		boolean categoryMatches = request.category().isEmpty() || request.category().contains(store.getCategory());
		boolean openMatches = !request.openNow() || store.businessStatusAt(now) == BusinessStatus.OPEN;
		return categoryMatches && openMatches;
	}

	private record Entry(StoreWithDistance item, OffsetDateTime bookmarkedAt) {

		static final Comparator<Entry> BY_DISTANCE = Comparator.comparing(entry -> entry.item().distance());

		static Entry of(Bookmark bookmark, Optional<GeoPoint> origin) {
			Store store = bookmark.getStore();
			Integer distance = origin.map(point -> point.distanceMetersTo(GeoPoint.from(store.getLocation())))
					.orElse(null);
			return new Entry(new StoreWithDistance(store, distance),
					bookmark.getCreatedAt().atZone(ZONE).toOffsetDateTime());
		}

	}

}
