package com.lasteat.app.ui.owner

// UI emits requests. Authentication, uploads, validation and persistence belong to the host.
enum class OwnerSection { SHOP, MENUS, HOURS, NOTICES, REVIEWS, STATS, ALERTS }
data class ShopDraft(val name: String, val introduction: String, val address: String, val phone: String, val photoUrl: String)
data class HoursDraft(val opens: String, val closes: String, val closedDays: Set<Int>)
data class MenuDraft(val name: String, val price: Int, val description: String, val photoUrl: String)
data class NoticeDraft(val headline: String, val condition: String, val periodText: String)
sealed interface OwnerAction {
    data class LoadSection(val section: OwnerSection) : OwnerAction
    data class SaveShop(val draft: ShopDraft) : OwnerAction
    data class SaveHours(val draft: HoursDraft) : OwnerAction
    data class SetBusinessStatus(val status: BusinessStatus) : OwnerAction
    data class SaveMenu(val id: String?, val draft: MenuDraft) : OwnerAction
    data class SetMenuSoldOut(val id: String, val soldOut: Boolean) : OwnerAction
    data class DeleteMenu(val id: String) : OwnerAction
    data class SaveNotice(val id: String?, val draft: NoticeDraft) : OwnerAction
    data class SetNoticePublished(val id: String, val isPublished: Boolean) : OwnerAction
    data class DeleteNotice(val id: String) : OwnerAction
    data class SaveReviewReply(val reviewId: String, val text: String) : OwnerAction
}
