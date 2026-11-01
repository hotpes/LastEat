package com.lasteat.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lasteat.app.data.Restaurant
import com.lasteat.app.ui.components.ChipRow
import com.lasteat.app.ui.components.LastEatTopBar
import com.lasteat.app.ui.components.RestaurantCard
import com.lasteat.app.ui.components.TintedBanner
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.LastEatColors
import com.lasteat.app.ui.theme.Spacing

private val CategoryFilters = listOf("전체", "한식", "양식", "일식", "카페")

/**
 * 주변 탐색 탭.
 * 지도 SDK(네이버/카카오 지도)는 아직 정해지지 않아 MapPlaceholder 자리에 표시만 해 두었습니다.
 * SDK 가 정해지면 MapPlaceholder 하나만 교체하면 됩니다.
 */
@Composable
fun ExploreScreen(
    locationName: String,
    restaurants: List<Restaurant>,
    savedIds: Set<String>,
    onToggleSave: (String) -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    onBenefitsClick: () -> Unit,
    onSeeAllClick: () -> Unit,
) {
    var category by rememberSaveable { mutableStateOf("전체") }
    val filtered = remember(category, restaurants) {
        if (category == "전체") restaurants else restaurants.filter { it.category == category }
    }

    LazyColumn(Modifier.fillMaxSize()) {
        item { LastEatTopBar(locationName, onLocationClick) }

        item {
            Box(Modifier.fillMaxWidth().height(320.dp)) {
                MapPlaceholder(restaurants, Modifier.fillMaxSize())
                Column(Modifier.fillMaxWidth()) {
                    SearchField(onSearchClick, Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md))
                    ChipRow(CategoryFilters, category, { category = it })
                }
            }
        }

        item {
            Row(
                Modifier.padding(start = Spacing.Screen, end = Spacing.Screen, top = Spacing.xl, bottom = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("가까운 곳에서, 맛있는 발견", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "내 주변 500m · 지금 영업 중인 ${filtered.count { it.isOpen }}곳",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(LastEatIcons.Sliders, "정렬·필터", Modifier.size(22.dp))
            }
        }

        if (filtered.isEmpty()) {
            item {
                Text(
                    "이 카테고리의 식당이 아직 없어요. 다른 카테고리를 골라 보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.Screen),
                )
            }
        } else {
            items(filtered.take(3), key = { it.id }) { r ->
                RestaurantCard(
                    restaurant = r,
                    saved = r.id in savedIds,
                    onClick = { onRestaurantClick(r) },
                    onToggleSave = { onToggleSave(r.id) },
                )
            }
        }

        item {
            TintedBanner(
                title = "맛있는 동네, 반가운 혜택",
                description = "$locationName 제휴 식당의 혜택을 만나보세요",
                icon = LastEatIcons.Ticket,
                onClick = onBenefitsClick,
                modifier = Modifier.padding(horizontal = Spacing.Screen, vertical = Spacing.md),
            )
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSeeAllClick)
                    .padding(vertical = Spacing.lg),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("주변 식당 모두 보기", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(4.dp))
                Icon(LastEatIcons.ArrowRight, null, Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SearchField(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp).shadow(2.dp, MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(Modifier.padding(horizontal = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(LastEatIcons.Search, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text("오늘은 어떤 맛을 찾으세요?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 지도 SDK 연결 전 자리 표시. 마커 위치는 시안 배치를 흉내 낸 고정값입니다. */
@Composable
private fun MapPlaceholder(restaurants: List<Restaurant>, modifier: Modifier = Modifier) {
    val spots: List<Pair<Dp, Dp>> = listOf(130.dp to 150.dp, 20.dp to 230.dp, 230.dp to 245.dp, 24.dp to 108.dp)
    Box(modifier.background(LastEatColors.MapBase)) {
        // 현재 위치 점
        Box(
            Modifier
                .offset(160.dp, 205.dp)
                .size(16.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(LastEatColors.White)
                .padding(3.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Color4285F4),
        )
        restaurants.zip(spots).forEachIndexed { index, (r, pos) ->
            MapMarker(r.name, r.rating, highlighted = index == 0, modifier = Modifier.offset(pos.first, pos.second))
        }
        Surface(
            onClick = {},
            modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg).size(40.dp),
            shape = MaterialTheme.shapes.extraLarge,
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(LastEatIcons.Locate, "내 위치", Modifier.size(20.dp))
            }
        }
        Surface(
            onClick = {},
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.lg),
            shape = MaterialTheme.shapes.extraLarge,
            shadowElevation = 2.dp,
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(LastEatIcons.Refresh, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("이 지역 다시 찾기", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private val Color4285F4 = androidx.compose.ui.graphics.Color(0xFF4285F4)

@Composable
private fun MapMarker(name: String, rating: Double, highlighted: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (highlighted) colors.primary else colors.surface,
        contentColor = if (highlighted) colors.onPrimary else colors.onSurface,
        border = if (highlighted) null else BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(LastEatIcons.Utensils, null, Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text("$name ${"%.1f".format(rating)}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
