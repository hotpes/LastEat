package com.lasteat.owner

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

internal val Forest = Color(0xFF25744F)
internal val DeepForest = Color(0xFF19563C)
internal val Sage = Color(0xFFEAF3E8)
internal val Ink = Color(0xFF26332D)
internal val Muted = Color(0xFF78847B)
internal val CanvasColor = Color(0xFFF6F8F4)
internal val Line = Color(0xFFE5EAE3)
internal val Amber = Color(0xFFBF8535)

@Composable internal fun OwnerTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.typography // Keep every font family supplied by the main app.
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Forest, onPrimary = Color.White, primaryContainer = Sage, onPrimaryContainer = DeepForest, secondary = Forest, secondaryContainer = Sage, onSecondaryContainer = DeepForest, tertiary = Forest, tertiaryContainer = Sage, onTertiaryContainer = DeepForest, background = CanvasColor, onBackground = Ink, surface = Color.White, onSurface = Ink, surfaceVariant = Sage, onSurfaceVariant = Muted, surfaceContainerLowest = Color.White, surfaceContainerLow = CanvasColor, surfaceContainer = CanvasColor, surfaceContainerHigh = Sage, surfaceContainerHighest = Sage, surfaceTint = Forest, outline = Line, error = Color(0xFFAC433E)),
        typography = base.copy(
            headlineSmall = base.headlineSmall.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 35.sp),
            titleLarge = base.titleLarge.copy(fontSize = 21.sp, fontWeight = FontWeight.Bold),
            titleMedium = base.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            titleSmall = base.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            bodyLarge = base.bodyLarge.copy(fontSize = 14.sp, lineHeight = 22.sp),
            bodyMedium = base.bodyMedium.copy(fontSize = 13.sp, lineHeight = 21.sp),
            bodySmall = base.bodySmall.copy(fontSize = 11.sp, lineHeight = 18.sp),
            labelLarge = base.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            labelMedium = base.labelMedium.copy(fontSize = 12.sp),
            labelSmall = base.labelSmall.copy(fontSize = 11.sp),
        ),
        shapes = Shapes(small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(20.dp)),
        content = content,
    )
}

enum class OwnerPanel { NONE, REDEEM, SHOP, MENUS, HOURS, BENEFIT, REVIEWS, STATS, NOTICES, ALERTS, STATUS }

@Composable internal fun WhiteCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(17.dp), content = content)
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

@Composable fun OwnerPage(state: OwnerState, onAction: (OwnerPanel) -> Unit, onOffer: (String) -> Unit, onPause: (String) -> Unit, modifier: Modifier = Modifier) {
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
        Surface(onClick = { onAction(OwnerPanel.REDEEM) }, color = Forest, shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(OwnerIcons.QrCodeScanner, null, Modifier.size(25.dp), tint = Color.White) }
                Column(Modifier.weight(1f).padding(start = 13.dp)) {
                    Text("손님이 쿠폰을 가져오셨나요?", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD4E8DB))
                    Text("쿠폰 확인·사용 처리", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Icon(OwnerIcons.ChevronRight, null, Modifier.size(23.dp), tint = Color.White)
            }
        }
        SectionTitle("가게 관리")
        val actions = listOf(
            Triple("가게 정보", OwnerIcons.Storefront, OwnerPanel.SHOP), Triple("메뉴 관리", OwnerIcons.RestaurantMenu, OwnerPanel.MENUS), Triple("영업시간", OwnerIcons.Schedule, OwnerPanel.HOURS),
            Triple("혜택 만들기", OwnerIcons.ConfirmationNumber, OwnerPanel.BENEFIT), Triple("리뷰 관리", OwnerIcons.RateReview, OwnerPanel.REVIEWS), Triple("가게 소식", OwnerIcons.Campaign, OwnerPanel.NOTICES),
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
                Metric("쿠폰 사용 누적", state.stats.usedTotal, OwnerIcons.ConfirmationNumber, Modifier.weight(1f))
            }
            Spacer(Modifier.height(13.dp))
            Text("가게를 찾은 손님과 혜택 반응을 확인해요", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        SectionTitle("우리 가게 혜택", "+ 만들기") { onAction(OwnerPanel.BENEFIT) }
        state.offers.forEachIndexed { index, offer ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            OfferCard(offer, onEdit = { onOffer(offer.id) }, onPause = { onPause(offer.id) })
        }
        if (state.offers.isEmpty()) Text("첫 혜택을 만들어 주변 손님에게 가게를 알려보세요.", color = Muted)
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
@Composable internal fun OfferCard(offer: Offer, onEdit: () -> Unit, onPause: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Sage).padding(17.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(OwnerIcons.ConfirmationNumber, null, Modifier.size(18.dp), tint = Forest)
            Text("  ${offer.statusText}", style = MaterialTheme.typography.labelSmall, color = Forest, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(OwnerIcons.Edit, "혜택 수정: ${offer.title}", Modifier.size(18.dp), tint = Forest) }
        }
        Text(offer.title, style = MaterialTheme.typography.titleLarge, color = DeepForest, modifier = Modifier.padding(top = 6.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(offer.terms.description(), color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
        Text(offer.terms.scheduleLabel(), color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("${offer.terms.expires.replace('-', '.')}까지", color = Forest, style = MaterialTheme.typography.bodySmall)
        HorizontalDivider(Modifier.padding(vertical = 13.dp), color = Forest.copy(alpha = .12f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("발급 ${offer.issued}  ·  사용 ${offer.used}", style = MaterialTheme.typography.labelMedium, color = DeepForest, modifier = Modifier.weight(1f))
            TextButton(onClick = onPause, enabled = offer.canToggleIssuance && !LocalOwnerSubmitting.current, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.heightIn(min = 44.dp)) {
                Icon(if (offer.paused) OwnerIcons.PlayArrow else OwnerIcons.Pause, null, Modifier.size(15.dp))
                Text(if (offer.paused) "발급 재개" else "발급 중지", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
