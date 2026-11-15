package com.lasteat.global.response;

import java.util.List;
import org.springframework.data.domain.Page;

// 목록 응답. DB 페이지는 from(Page), 메모리에서 거른 목록은 slice로 자른 뒤 of로 만든다.
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, boolean hasNext) {

	public PageResponse {
		content = List.copyOf(content);
	}

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.hasNext());
	}

	public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
		return new PageResponse<>(content, page, size, totalElements, (long) (page + 1) * size < totalElements);
	}

	// 전체 목록에서 page번째 묶음. 범위를 넘으면 빈 목록
	public static <T> List<T> slice(List<T> all, int page, int size) {
		long from = (long) page * size;
		if (from >= all.size()) {
			return List.of();
		}
		return all.subList((int) from, (int) Math.min(all.size(), from + size));
	}

}
