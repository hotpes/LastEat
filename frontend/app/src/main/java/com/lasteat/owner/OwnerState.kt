package com.lasteat.owner

import java.time.LocalDate
import java.util.Locale

// Display models only. The host supplies server-owned values to OwnerScreen.
enum class BusinessStatus(val label: String) { OPEN("영업 중"), BREAK("준비 중"), CLOSED("오늘 휴무") }
data class Shop(
    val name: String = "", val introduction: String = "", val address: String = "", val phone: String = "",
    val opens: String = "", val closes: String = "", val closedDays: Set<Int> = emptySet(),
    val status: BusinessStatus? = null, val photoUrl: String = "",
    val categoryLabel: String = "", val hoursSummary: String = "",
)
data class MenuItem(val id: String, val name: String, val price: Int, val description: String, val soldOut: Boolean = false, val photoUrl: String = "")
enum class BenefitKind(val label: String) { PERCENT("비율 할인"), FIXED("금액 할인"), FREE_ITEM("서비스 제공") }
data class BenefitTerms(
    val percent: Int, val minimum: Int, val maximum: Int, val expires: String,
    val kind: BenefitKind, val amount: Int, val freeItem: String,
    val days: Set<Int>, val starts: String, val ends: String,
) {
    // Text formatting, not eligibility checks or discount calculation.
    fun headline() = when (kind) { BenefitKind.PERCENT -> "식사 금액 ${percent}% 할인"; BenefitKind.FIXED -> "식사 금액 ${amount.won()} 할인"; BenefitKind.FREE_ITEM -> "$freeItem 무료" }
    fun description() = "${minimum.won()} 이상" + if (kind == BenefitKind.PERCENT) " · " + (if (maximum == 0) "최대 할인 제한 없음" else "최대 ${maximum.won()} 할인") else " · 1인 1회 사용"
    fun scheduleLabel(): String {
        val names = listOf("월", "화", "수", "목", "금", "토", "일")
        val dayText = if (days.size == 7) "매일" else days.sorted().mapNotNull { names.getOrNull(it - 1) }.joinToString("·")
        return "$dayText · " + if (starts == "00:00" && ends == "23:59") "하루 종일" else "$starts–$ends"
    }
}
data class Offer(val id: String, val title: String, val terms: BenefitTerms, val quantity: Int, val issued: Int = 0, val used: Int = 0, val paused: Boolean = false, val statusText: String = "발급 중", val canToggleIssuance: Boolean = true)
data class Review(val id: String, val nickname: String, val stars: Int, val date: String, val text: String, val reply: String = "")
data class Notice(val title: String, val body: String, val date: String)
data class DailyViews(val dayLabel: String, val count: Int)
data class OwnerStats(val viewsToday: String = "—", val directionsToday: String = "—", val issuedTotal: String = "—", val usedTotal: String = "—", val useRate: String = "—", val dailyViews: List<DailyViews> = emptyList())
data class ReviewSummary(val rating: String = "—", val total: Int = 0, val pending: Int = 0, val featured: Review? = null)
data class CouponDetails(val id: String, val maskedCode: String, val title: String, val customerLabel: String, val conditionText: String, val scheduleText: String, val expiresText: String)
data class CouponQuote(val id: String, val orderAmount: Int, val discountAmount: Int, val payableAmount: Int, val canRedeem: Boolean)
sealed interface CouponUiState {
    data object Idle : CouponUiState
    data object Checking : CouponUiState
    data class Rejected(val message: String) : CouponUiState
    data class Ready(val coupon: CouponDetails, val quote: CouponQuote? = null, val quoteLoading: Boolean = false, val error: String? = null) : CouponUiState
    data class Redeemed(val title: String, val message: String) : CouponUiState
}
data class OwnerResult(val id: String, val success: Boolean, val message: String)
data class OwnerState(
    val shop: Shop = Shop(), val menus: List<MenuItem> = emptyList(), val offers: List<Offer> = emptyList(),
    val reviews: List<Review> = emptyList(), val notices: List<Notice> = emptyList(),
    val stats: OwnerStats = OwnerStats(), val reviewSummary: ReviewSummary = ReviewSummary(),
    val coupon: CouponUiState = CouponUiState.Idle,
    val isLoading: Boolean = false, val isSubmitting: Boolean = false, val result: OwnerResult? = null,
)
fun Int.won() = String.format(Locale.KOREA, "%,d원", this)
fun String.moneyValue(): Int? = replace(",", "").trim().toIntOrNull()?.takeIf { it >= 0 }
fun normalizeCode(value: String) = value.trim().uppercase(Locale.ROOT)
fun hoursLabel(shop: Shop) = shop.hoursSummary.ifBlank {
    if (shop.opens.isBlank() || shop.closes.isBlank()) "영업시간 미등록" else "${shop.opens}–${shop.closes}"
}
// Only input shape is checked here. The backend owns all business validation.
fun validateHours(opens: String, closes: String): String? {
    val format = Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")
    return if (!format.matches(opens) || !format.matches(closes)) "시간을 11:00처럼 입력해 주세요." else null
}
fun validateBenefitInput(terms: BenefitTerms): String? = when {
    terms.kind == BenefitKind.PERCENT && terms.percent < 0 -> "할인율을 숫자로 입력해 주세요."
    terms.kind == BenefitKind.FIXED && terms.amount < 0 -> "할인 금액을 숫자로 입력해 주세요."
    terms.kind == BenefitKind.FREE_ITEM && terms.freeItem.isBlank() -> "제공할 메뉴를 입력해 주세요."
    terms.minimum < 0 || terms.maximum < 0 -> "금액을 숫자로 입력해 주세요."
    runCatching { LocalDate.parse(terms.expires) }.isFailure -> "종료일 형식을 확인해 주세요."
    terms.days.isEmpty() -> "사용 가능한 요일을 선택해 주세요."
    validateHours(terms.starts, terms.ends) != null -> "사용 시간을 HH:MM 형식으로 입력해 주세요."
    else -> null
}
