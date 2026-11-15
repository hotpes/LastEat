package com.lasteat.app.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Read colors and typography from the main app's LastEatTheme.
internal val Forest: Color @Composable get() = MaterialTheme.colorScheme.primary
internal val DeepForest: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
internal val Sage: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
internal val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
internal val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
internal val CanvasColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
internal val Line: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
internal val Amber = Color(0xFFE5A21A)
@Composable internal fun OwnerTheme(content: @Composable () -> Unit) { content() }
enum class OwnerPanel { NONE, SHOP, MENUS, HOURS, NOTICE_EDITOR, NOTICES, REVIEWS, STATS, ALERTS, STATUS }

@Composable internal fun WhiteCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(17.dp), content = content)
}
@Composable internal fun Badge(text: String, color: Color = Forest, background: Color = Sage) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(background).padding(horizontal = 8.dp, vertical = 4.dp))
}
@Composable internal fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 0.dp)) { Text(action, style = MaterialTheme.typography.labelMedium); Icon(OwnerIcons.ChevronRight, null, Modifier.size(16.dp)) }
    }
}
@Composable internal fun SmallIcon(icon: ImageVector, modifier: Modifier = Modifier, color: Color = Forest) {
    Box(modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Sage), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(21.dp), tint = color) }
}
/** The host uses its existing image loader (server URL in, Compose content out). */
typealias OwnerImageContent = @Composable (url: String, description: String, modifier: Modifier) -> Unit

/** The host selects/uploads a photo, then returns the final display URL to the draft. */
typealias OwnerPickImage = (currentUrl: String, onSelectedUrl: (String) -> Unit) -> Unit

internal val LocalOwnerImageContent = staticCompositionLocalOf<OwnerImageContent> {
    { url, description, modifier -> OwnerImagePlaceholder(url, description, modifier) }
}
internal val LocalOwnerPickImage = staticCompositionLocalOf<OwnerPickImage?> { null }

@Composable internal fun MenuPhoto(url: String, modifier: Modifier = Modifier) = OwnerPhoto(url, "메뉴 사진", modifier)
@Composable internal fun ShopPhoto(url: String, modifier: Modifier = Modifier) = OwnerPhoto(url, "가게 대표 사진", modifier)
@Composable private fun OwnerPhoto(url: String, description: String, modifier: Modifier) {
    LocalOwnerImageContent.current(url, description, modifier.clip(RoundedCornerShape(14.dp)))
}

/** Placeholder only: no bundled assets, local decoding or network work. */
@Composable internal fun OwnerImagePlaceholder(url: String, description: String, modifier: Modifier) {
    Box(modifier.background(Sage).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(OwnerIcons.Image, null, Modifier.size(24.dp), tint = Forest.copy(alpha = .65f))
            Text(if (url.isBlank()) "사진" else "이미지 영역", color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable fun OwnerPage(state: OwnerState, onAction: (OwnerPanel) -> Unit, onNewNotice: () -> Unit, onNoticeEdit: (String) -> Unit, onNoticeVisibility: (String, Boolean) -> Unit, onNoticeDelete: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 26.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("사장님, 반가워요", color = Muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text("오늘도 맛있는 만남을 이어가요.", style = MaterialTheme.typography.titleLarge, fontSize = 20.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShopPhoto(state.shop.photoUrl, Modifier.size(72.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Badge("내 가게")
                    Spacer(Modifier.height(5.dp))
                    Text(state.shop.name.ifBlank { "가게 정보를 등록해 주세요" }, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(state.shop.categoryLabel, color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { onAction(OwnerPanel.SHOP) }) { Icon(OwnerIcons.Edit, "가게 정보 수정", Modifier.size(20.dp), tint = Muted) }
            }
            HorizontalDivider(Modifier.padding(vertical = 15.dp), color = Line)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(OwnerIcons.Schedule, null, Modifier.size(16.dp), tint = Muted)
                Text("  ${hoursLabel(state.shop)}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Surface(onClick = { onAction(OwnerPanel.STATUS) }, shape = RoundedCornerShape(24.dp), color = if (state.shop.status == BusinessStatus.OPEN) Sage else CanvasColor) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(if (state.shop.status == BusinessStatus.OPEN) Forest else Muted))
                        Text(state.shop.status?.label ?: "상태 선택", color = if (state.shop.status == BusinessStatus.OPEN) Forest else Muted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp))
                        Icon(OwnerIcons.ExpandMore, null, Modifier.size(16.dp), tint = Forest)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Surface(onClick = onNewNotice, color = Forest, shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(OwnerIcons.Campaign, null, Modifier.size(25.dp), tint = Color.White) }
                Column(Modifier.weight(1f).padding(start = 13.dp)) {
                    Text("우리 가게의 할인 소식을 알려보세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .85f))
                    Text("할인 공지 등록", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Icon(OwnerIcons.ChevronRight, null, Modifier.size(23.dp), tint = Color.White)
            }
        }
        SectionTitle("가게 관리")
        val actions = listOf(
            Triple("가게 정보", OwnerIcons.Storefront, OwnerPanel.SHOP), Triple("메뉴 관리", OwnerIcons.RestaurantMenu, OwnerPanel.MENUS), Triple("영업시간", OwnerIcons.Schedule, OwnerPanel.HOURS),
            Triple("공지 관리", OwnerIcons.Campaign, OwnerPanel.NOTICES), Triple("리뷰 관리", OwnerIcons.RateReview, OwnerPanel.REVIEWS), Triple("통계 보기", OwnerIcons.Visibility, OwnerPanel.STATS),
        )
        actions.chunked(3).forEachIndexed { index, row ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (title, icon, panel) ->
                    Surface(onClick = { onAction(panel) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                        Column(Modifier.padding(horizontal = 4.dp, vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(icon, null, Modifier.size(23.dp), tint = Forest)
                            Spacer(Modifier.height(9.dp))
                            Text(title, style = MaterialTheme.typography.labelMedium, color = Ink)
                        }
                    }
                }
            }
        }
        SectionTitle("오늘의 가게", "자세히") { onAction(OwnerPanel.STATS) }
        WhiteCard {
            Row(Modifier.fillMaxWidth()) {
                Metric("가게 조회", state.stats.viewsToday, OwnerIcons.Visibility, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(60.dp).background(Line))
                Metric("길찾기 클릭", state.stats.directionsToday, OwnerIcons.NearMe, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(60.dp).background(Line))
                Metric("할인 공지 조회", state.stats.noticeViewsToday, OwnerIcons.Campaign, Modifier.weight(1f))
            }
            Spacer(Modifier.height(13.dp))
            Text("가게를 찾은 손님과 혜택 반응을 확인해요", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        SectionTitle("우리 가게 할인 공지", "전체 보기") { onAction(OwnerPanel.NOTICES) }
        val notice = state.notices.firstOrNull { it.isPublished } ?: state.notices.firstOrNull()
        if (notice == null) {
            WhiteCard {
                Text("등록된 할인 공지가 없어요.", color = Muted)
                TextButton(onClick = onNewNotice) { Text("첫 할인 공지 등록하기"); Icon(OwnerIcons.ChevronRight, null) }
            }
        } else NoticeCard(notice, { onNoticeEdit(notice.id) }, { onNoticeVisibility(notice.id, !notice.isPublished) }, { onNoticeDelete(notice.id) })
        val pending = state.reviewSummary.pending
        SectionTitle("손님이 남긴 이야기", "전체 ${state.reviewSummary.total}") { onAction(OwnerPanel.REVIEWS) }
        WhiteCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(OwnerIcons.Star, null, Modifier.size(19.dp), tint = Amber)
                Text(" ${state.reviewSummary.rating}", style = MaterialTheme.typography.titleLarge)
                Text("  ·  리뷰 ${state.reviewSummary.total}개", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                if (pending > 0) Badge("답글 대기 $pending") else if (state.reviewSummary.total > 0) Badge("답글 완료")
            }
            val review = state.reviewSummary.featured
            if (review != null) {
                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Line)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(Sage), contentAlignment = Alignment.Center) { Text(review.nickname.take(1), color = Forest, style = MaterialTheme.typography.labelMedium) }
                    Text("  ${review.nickname}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    Text("★".repeat(review.stars), color = Amber, fontSize = 11.sp)
                }
                Text(review.text, modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                OutlinedButton(onClick = { onAction(OwnerPanel.REVIEWS) }, modifier = Modifier.fillMaxWidth().padding(top = 13.dp), shape = RoundedCornerShape(10.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line)) { Icon(OwnerIcons.ModeComment, null, Modifier.size(16.dp)); Text(if (pending == 0) "답글 확인하기" else "따뜻한 답글 남기기", Modifier.padding(start = 6.dp)) }
            }
        }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable private fun Metric(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
        Text(value, style = MaterialTheme.typography.titleLarge, fontSize = 25.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
@Composable internal fun NoticeCard(notice: Notice, onEdit: () -> Unit, onVisibility: () -> Unit, onDelete: () -> Unit) {
    var deleting by remember { mutableStateOf(false) }
    val enabled = notice.canManage && !LocalOwnerSubmitting.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Sage).padding(17.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(OwnerIcons.Campaign, null, Modifier.size(18.dp), tint = Forest)
            Text(if (notice.isPublished) "  게시 중" else "  비공개", style = MaterialTheme.typography.labelSmall, color = Forest, modifier = Modifier.weight(1f))
            if (notice.canManage) IconButton(onClick = onEdit, enabled = enabled) { Icon(OwnerIcons.Edit, "공지 수정: ${notice.headline}", Modifier.size(18.dp), tint = Forest) }
        }
        Text(notice.headline, style = MaterialTheme.typography.titleLarge, color = DeepForest)
        Text(notice.condition, color = Ink, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        Text("안내 기간  ${notice.periodText}", color = Forest, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        if (notice.publishedAt.isNotBlank()) Text(notice.publishedAt, color = Muted, style = MaterialTheme.typography.bodySmall)
        if (notice.canManage) {
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Line)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onVisibility, enabled = enabled) { Text(if (notice.isPublished) "비공개로 전환" else "공지 공개") }
                TextButton(onClick = { deleting = true }, enabled = enabled) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    if (deleting) AlertDialog(onDismissRequest = { deleting = false }, title = { Text("공지를 삭제할까요?") }, text = { Text(notice.headline) }, confirmButton = { TextButton(onClick = { deleting = false; onDelete() }, enabled = enabled) { Text("삭제") } }, dismissButton = { TextButton(onClick = { deleting = false }) { Text("취소") } })
}
