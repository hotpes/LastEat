package com.lasteat.app.ui.owner

import java.util.Locale

enum class BusinessStatus(val label: String) { OPEN("영업 중"), BREAK("준비 중"), CLOSED("오늘 휴무") }
data class Shop(
    val name: String = "", val introduction: String = "", val address: String = "", val phone: String = "",
    val opens: String = "", val closes: String = "", val closedDays: Set<Int> = emptySet(),
    val status: BusinessStatus? = null, val photoUrl: String = "",
    val categoryLabel: String = "", val hoursSummary: String = "",
)
data class MenuItem(val id: String, val name: String, val price: Int, val description: String, val soldOut: Boolean = false, val photoUrl: String = "")

/** One text notice format. These three display fields match the main app's Benefit card. */
data class Notice(
    val id: String, val headline: String, val condition: String, val periodText: String,
    val isPublished: Boolean, val publishedAt: String = "", val canManage: Boolean = true,
)
data class Review(val id: String, val nickname: String, val stars: Int, val date: String, val text: String, val reply: String = "")
data class DailyViews(val dayLabel: String, val count: Int)
data class OwnerStats(
    val viewsToday: String = "—", val directionsToday: String = "—", val noticeViewsToday: String = "—",
    val noticeCount: String = "—", val dailyViews: List<DailyViews> = emptyList(),
)
data class ReviewSummary(val rating: String = "—", val total: Int = 0, val pending: Int = 0, val featured: Review? = null)
data class OwnerResult(val id: String, val success: Boolean, val message: String)
data class OwnerState(
    val shop: Shop = Shop(), val menus: List<MenuItem> = emptyList(), val notices: List<Notice> = emptyList(),
    val reviews: List<Review> = emptyList(), val reviewSummary: ReviewSummary = ReviewSummary(),
    val stats: OwnerStats = OwnerStats(), val isLoading: Boolean = false, val isSubmitting: Boolean = false,
    val result: OwnerResult? = null,
)
fun Int.won() = String.format(Locale.KOREA, "%,d원", this)
fun String.moneyValue(): Int? = replace(",", "").trim().toIntOrNull()?.takeIf { it >= 0 }
fun hoursLabel(shop: Shop) = shop.hoursSummary.ifBlank {
    if (shop.opens.isBlank() || shop.closes.isBlank()) "영업시간 미등록" else "${shop.opens}–${shop.closes}"
}
fun validateHours(opens: String, closes: String): String? {
    val format = Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")
    return if (!format.matches(opens) || !format.matches(closes)) "시간을 HH:MM 형식으로 입력해 주세요." else null
}
