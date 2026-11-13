package com.lasteat.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.lasteat.store.dto.StoreSummaryResponse;
import java.time.OffsetDateTime;

// 주변 가게 목록 항목과 같은 필드 + bookmarkedAt
public record BookmarkedStoreResponse(@JsonUnwrapped StoreSummaryResponse store, OffsetDateTime bookmarkedAt) {
}
