package com.lasteat.store.dto;

import com.lasteat.store.entity.Menu;

public record MenuResponse(Long id, String name, String description, int price, String imageUrl, boolean signature,
		boolean soldOut) {

	public static MenuResponse from(Menu menu) {
		return new MenuResponse(menu.getId(), menu.getName(), menu.getDescription(), menu.getPrice(),
				menu.getImageUrl(), menu.isSignature(), menu.isSoldOut());
	}

}
