package com.lasteat.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lasteat.app.data.Restaurant
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.LastEatColors
import com.lasteat.app.ui.theme.Spacing

// ─────────────────────────────────────────────────────────────
// 이미지 슬롯
// 사장님 UI(OwnerScreen)의 imageContent 와 같은 시그니처입니다.
// 이미지 로더(Coil 등)를 정하면 MainActivity 에서 한 번만 주입하고,
// 같은 람다를 OwnerScreen(imageContent = ...) 에도 넘기세요.
// ─────────────────────────────────────────────────────────────
typealias ImageContent = @Composable (url: String?, description: String?, modifier: Modifier) -> Unit

val DefaultImageContent: ImageContent = { _, description, modifier ->
    ImagePlaceholder(description, modifier)
}

val LocalImageContent = compositionLocalOf { DefaultImageContent }

@Composable
fun RemoteImage(url: String?, description: String?, modifier: Modifier = Modifier) {
    LocalImageContent.current(url, description, modifier)
}

@Composable
fun ImagePlaceholder(description: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(LastEatColors.Green50),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LastEatIcons.Utensils,
            contentDescription = description,
            tint = LastEatColors.Green500.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 상단 바: 로고 + 동네 선택 (주변 탐색 · 저장 · 마이)
// ─────────────────────────────────────────────────────────────
@Composable
fun LastEatTopBar(
    locationName: String,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = Spacing.Screen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(LastEatIcons.Utensils, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(Spacing.sm))
        Text("LastEat", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.weight(1f))
        LocationSelector(locationName, onLocationClick)
    }
}

@Composable
fun LocationSelector(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(LastEatIcons.Pin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(2.dp))
        Text(name, style = MaterialTheme.typography.titleSmall)
        Icon(LastEatIcons.ChevronDown, "동네 변경", modifier = Modifier.size(16.dp))
    }
}

/** 제목 + 부제가 있는 탭 화면 헤더 (저장, 마이, 혜택·이벤트) */
@Composable
fun ScreenTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Spacing.Screen, vertical = Spacing.sm)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 칩
// ─────────────────────────────────────────────────────────────
@Composable
fun LastEatChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 36.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) colors.primary else colors.surface,
        contentColor = if (selected) colors.onPrimary else colors.onSurface,
        border = if (selected) null else BorderStroke(1.dp, colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = if (selected && leadingIcon == null) LastEatIcons.Check else leadingIcon
            if (icon != null && (selected || leadingIcon != null)) {
                Icon(icon, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** 가로 스크롤 필터 칩 줄. 하나만 선택되는 형태입니다. */
@Composable
fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.Screen),
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        options.forEach { option ->
            LastEatChip(option, option == selected, onClick = { onSelect(option) })
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 배지 · 별점
// ─────────────────────────────────────────────────────────────
@Composable
fun BenefitBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
fun RatingRow(rating: Double, reviewCount: Int, isOpen: Boolean? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(LastEatIcons.Star, null, tint = LastEatColors.Star, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(3.dp))
        Text("%.1f".format(rating), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.width(3.dp))
        Text("($reviewCount)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (isOpen != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                if (isOpen) "영업 중" else "영업 종료",
                style = MaterialTheme.typography.bodySmall,
                color = if (isOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun formatDistance(meters: Int): String =
    if (meters < 1000) "${meters}m" else "%.1fkm".format(meters / 1000.0)

// ─────────────────────────────────────────────────────────────
// 식당 카드 (주변 탐색 목록 · 저장 목록 공용)
// ─────────────────────────────────────────────────────────────
@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    showCollection: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Screen, vertical = Spacing.md),
    ) {
        RemoteImage(
            url = restaurant.photoUrl,
            description = "${restaurant.name} 대표 사진",
            modifier = Modifier
                .size(76.dp)
                .clip(MaterialTheme.shapes.small),
        )
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                restaurant.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${restaurant.category} · ${restaurant.menuSummary} · ${formatDistance(restaurant.distanceM)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            RatingRow(restaurant.rating, restaurant.reviewCount, restaurant.isOpen)
            val collection = restaurant.collectionName.takeIf { showCollection }
            if (restaurant.benefitBadge != null || collection != null) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (collection != null) {
                        Text(collection, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                    }
                    restaurant.benefitBadge?.let { BenefitBadge(it) }
                }
            }
        }
        IconButton(onClick = onToggleSave, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = if (saved) LastEatIcons.BookmarkFilled else LastEatIcons.Bookmark,
                contentDescription = if (saved) "저장 취소" else "저장",
                tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 버튼 · 연한 배너 · 빈 상태
// ─────────────────────────────────────────────────────────────
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    leadingIcon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = MaterialTheme.shapes.small,
        color = if (enabled) containerColor else MaterialTheme.colorScheme.outlineVariant,
        contentColor = if (enabled) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** 연한 그린 배경의 안내 카드 (반가운 혜택, 사장님 등록 진입 등) */
@Composable
fun TintedBanner(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                if (eyebrow != null) {
                    Text(eyebrow, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Icon(LastEatIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
    }
}

/** 빈 상태 / 로그인 필요 상태 공용 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.xs))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionText != null) {
            Spacer(Modifier.height(Spacing.xl))
            PrimaryButton(actionText, onAction, Modifier.width(220.dp))
        }
    }
}

@Composable
fun SectionDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
