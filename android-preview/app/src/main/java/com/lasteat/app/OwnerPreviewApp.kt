package com.lasteat.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import com.lasteat.app.ui.owner.*
import java.util.UUID

@Composable
internal fun OwnerPreviewApp(state: MutableState<OwnerState>, onClose: () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF246448), onPrimary = Color.White,
        primaryContainer = Color(0xFFE6F1E9), onPrimaryContainer = Color(0xFF173E2C),
        surface = Color.White, surfaceVariant = Color(0xFFF5F7F4),
        onSurface = Color(0xFF1D2923), onSurfaceVariant = Color(0xFF65716A),
        outlineVariant = Color(0xFFE1E7E2),
    )) {
        OwnerScreen(
            state = state.value,
            onAction = { state.value = reduce(state.value, it) },
            onClose = onClose,
        )
    }
}

internal fun sampleState() = OwnerState(
    shop = Shop(
        name = "라스트잇 샐러드", introduction = "오늘 만든 신선한 한 끼를 만나보세요.",
        address = "서울시 성동구 테스트로 10", phone = "02-000-0000",
        opens = "09:00", closes = "21:00", status = BusinessStatus.OPEN,
        categoryLabel = "샐러드·도시락",
    ),
    menus = listOf(
        MenuItem("menu-1", "치킨 샐러드", 8500, "닭가슴살과 신선한 채소"),
        MenuItem("menu-2", "두부 샐러드", 7500, "고소한 두부와 제철 채소"),
    ),
    notices = listOf(Notice("notice-1", "마감 30% 할인", "방문 포장 시 적용", "매일 19:00–21:00", true)),
    reviews = listOf(Review("review-1", "배고픈 손님", 5, "2026-11-10", "신선하고 맛있어요!")),
    reviewSummary = ReviewSummary("5.0", 1, 1),
    stats = OwnerStats("128", "24", "56", "1", listOf(DailyViews("월", 80), DailyViews("화", 128))),
)

// Preview host only. All edits are in memory; restarting the activity resets samples.
private fun reduce(current: OwnerState, action: OwnerAction): OwnerState {
    if (action is OwnerAction.LoadSection) return current.copy(result = null)
    val updated = when (action) {
        is OwnerAction.SaveShop -> action.draft.let { d ->
            current.copy(shop = current.shop.copy(name = d.name, introduction = d.introduction,
                address = d.address, phone = d.phone, photoUrl = d.photoUrl))
        }
        is OwnerAction.SaveHours -> action.draft.let { d ->
            current.copy(shop = current.shop.copy(opens = d.opens, closes = d.closes,
                closedDays = d.closedDays, hoursSummary = ""))
        }
        is OwnerAction.SetBusinessStatus -> current.copy(shop = current.shop.copy(status = action.status))
        is OwnerAction.SaveMenu -> {
            val d = action.draft
            val item = MenuItem(action.id ?: UUID.randomUUID().toString(), d.name, d.price,
                d.description, current.menus.find { it.id == action.id }?.soldOut ?: false, d.photoUrl)
            current.copy(menus = if (action.id == null) current.menus + item
                else current.menus.map { if (it.id == action.id) item else it })
        }
        is OwnerAction.SetMenuSoldOut -> current.copy(menus = current.menus.map {
            if (it.id == action.id) it.copy(soldOut = action.soldOut) else it
        })
        is OwnerAction.DeleteMenu -> current.copy(menus = current.menus.filterNot { it.id == action.id })
        is OwnerAction.SaveNotice -> {
            val d = action.draft
            val item = Notice(action.id ?: UUID.randomUUID().toString(), d.headline, d.condition,
                d.periodText, current.notices.find { it.id == action.id }?.isPublished ?: true)
            current.copy(notices = if (action.id == null) current.notices + item
                else current.notices.map { if (it.id == action.id) item else it })
        }
        is OwnerAction.SetNoticePublished -> current.copy(notices = current.notices.map {
            if (it.id == action.id) it.copy(isPublished = action.isPublished) else it
        })
        is OwnerAction.DeleteNotice -> current.copy(notices = current.notices.filterNot { it.id == action.id })
        is OwnerAction.SaveReviewReply -> current.copy(reviews = current.reviews.map {
            if (it.id == action.reviewId) it.copy(reply = action.text) else it
        })
        is OwnerAction.LoadSection -> current
    }
    return updated.copy(
        reviewSummary = updated.reviewSummary.copy(
            pending = updated.reviews.count { it.reply.isBlank() }, featured = updated.reviews.firstOrNull()),
        stats = updated.stats.copy(noticeCount = updated.notices.size.toString()),
        result = OwnerResult(UUID.randomUUID().toString(), true, "테스트 데이터에 반영했어요"),
    )
}
