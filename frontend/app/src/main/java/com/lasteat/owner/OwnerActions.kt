package com.lasteat.owner

// Backend integration boundary: UI emits intents; it does not execute them.
// photoUrl is the existing or newly uploaded server URL supplied by the host.
enum class OwnerSection { SHOP, MENUS, HOURS, BENEFITS, REVIEWS, STATS, NOTICES, ALERTS }
data class ShopDraft(val name: String, val introduction: String, val address: String, val phone: String, val photoUrl: String)
data class HoursDraft(val opens: String, val closes: String, val closedDays: Set<Int>)
data class MenuDraft(val name: String, val price: Int, val description: String, val photoUrl: String)
data class BenefitDraft(val terms: BenefitTerms, val quantity: Int)
sealed interface OwnerAction {
    data object OpenCoupon : OwnerAction
    data class LoadSection(val section: OwnerSection) : OwnerAction
    data class SaveShop(val draft: ShopDraft) : OwnerAction
    data class SaveHours(val draft: HoursDraft) : OwnerAction
    data class SetBusinessStatus(val status: BusinessStatus) : OwnerAction
    data class SaveMenu(val id: String?, val draft: MenuDraft) : OwnerAction
    data class SetMenuSoldOut(val id: String, val soldOut: Boolean) : OwnerAction
    data class DeleteMenu(val id: String) : OwnerAction
    data class SaveBenefit(val id: String?, val draft: BenefitDraft) : OwnerAction
    data class SetBenefitIssuance(val id: String, val paused: Boolean) : OwnerAction
    data class SaveReviewReply(val reviewId: String, val text: String) : OwnerAction
    data class PublishNotice(val title: String, val body: String) : OwnerAction
    data class CheckCoupon(val code: String) : OwnerAction
    data class QuoteCoupon(val couponId: String, val orderAmount: Int) : OwnerAction
    data class RedeemCoupon(val couponId: String, val quoteId: String) : OwnerAction
}
