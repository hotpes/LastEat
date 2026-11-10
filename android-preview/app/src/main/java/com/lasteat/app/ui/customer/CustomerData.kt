package com.lasteat.app.ui.customer

/** UI fixtures only. No API, authentication, location access or persistent storage. */
internal data class PreviewRestaurant(
    val id: String, val name: String, val category: String, val description: String,
    val distance: Int, val rating: String, val reviews: Int, val photo: String,
    val benefit: String = "", val collection: String = "든든한 한 끼",
)

internal val PreviewRestaurants = listOf(
    PreviewRestaurant("ongi", "연남 온기", "한식", "솥밥과 덮밥", 240, "4.8", 328,
        "a333f.png", "점심특가 10% 할인"),
    PreviewRestaurant("gyeol", "카페 결", "카페", "커피와 디저트", 320, "4.9", 186,
        "3d73b.png", collection = "분위기 좋은 카페"),
    PreviewRestaurant("sodam", "소담국수", "한식", "잔치국수와 비빔국수", 410, "4.6", 214,
        "a3f8d.png", "점심 세트 2,000원 할인"),
    PreviewRestaurant("green", "초록식탁", "양식", "샐러드와 파스타", 180, "4.7", 152,
        "b9da0.png"),
)
