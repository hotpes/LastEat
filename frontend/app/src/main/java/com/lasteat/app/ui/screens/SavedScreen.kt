package com.lasteat.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.lasteat.app.data.SavedCollection
import com.lasteat.app.data.Restaurant
import com.lasteat.app.data.UserSession
import com.lasteat.app.ui.components.ChipRow
import com.lasteat.app.ui.components.EmptyState
import com.lasteat.app.ui.components.LastEatTopBar
import com.lasteat.app.ui.components.RestaurantCard
import com.lasteat.app.ui.components.ScreenTitle
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.Spacing

private val SavedFilters = listOf("전체", "한식", "카페", "영업 중")

/**
 * 저장 탭.
 * 시안에서 비로그인(마이 탭)인데 저장 목록이 보이던 부분을 정리해서,
 * 비로그인일 때는 로그인 안내 빈 상태를 보여줍니다.
 */
@Composable
fun SavedScreen(
    session: UserSession,
    locationName: String,
    savedRestaurants: List<Restaurant>,
    collections: List<SavedCollection>,
    onLocationClick: () -> Unit,
    onLoginClick: () -> Unit,
    onExploreClick: () -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    onToggleSave: (String) -> Unit,
    onNewCollectionClick: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        LastEatTopBar(locationName, onLocationClick)
        ScreenTitle("저장", "다음에 갈 맛집, 한곳에 모아두세요")

        if (session is UserSession.Guest) {
            EmptyState(
                icon = LastEatIcons.Bookmark,
                title = "로그인하고 맛집을 모아보세요",
                description = "저장한 식당과 컬렉션은 로그인 후 어느 기기에서나 볼 수 있어요.",
                actionText = "로그인하기",
                onAction = onLoginClick,
            )
            return@Column
        }

        var filter by rememberSaveable { mutableStateOf("전체") }
        val shown = remember(filter, savedRestaurants) {
            when (filter) {
                "전체" -> savedRestaurants
                "영업 중" -> savedRestaurants.filter { it.isOpen }
                else -> savedRestaurants.filter { it.category == filter }
            }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.Screen, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("내 컬렉션 ${collections.size}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Surface(onClick = onNewCollectionClick, shape = MaterialTheme.shapes.extraLarge) {
                        Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(LastEatIcons.Plus, null, Modifier.size(14.dp))
                            Text("새 컬렉션", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.padding(horizontal = Spacing.Screen),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    collections.forEach { c -> CollectionCard(c, Modifier.weight(1f)) }
                }
            }
            item {
                ChipRow(SavedFilters, filter, { filter = it }, Modifier.padding(top = Spacing.xl, bottom = Spacing.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                Row(Modifier.padding(horizontal = Spacing.Screen, vertical = Spacing.md)) {
                    Text("저장한 식당 ", style = MaterialTheme.typography.titleSmall)
                    Text("${shown.size}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    Text("최근 저장순", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (shown.isEmpty()) {
                item {
                    EmptyState(
                        icon = LastEatIcons.Bookmark,
                        title = if (savedRestaurants.isEmpty()) "아직 저장한 식당이 없어요" else "조건에 맞는 식당이 없어요",
                        description = "마음에 드는 식당의 북마크를 눌러 저장해 보세요.",
                        actionText = "주변 식당 보러 가기",
                        onAction = onExploreClick,
                        modifier = Modifier.height(320.dp),
                    )
                }
            } else {
                items(shown, key = { it.id }) { r ->
                    RestaurantCard(
                        restaurant = r,
                        saved = true,
                        onClick = { onRestaurantClick(r) },
                        onToggleSave = { onToggleSave(r.id) },
                        showCollection = true,
                    )
                    HorizontalDivider(Modifier.padding(horizontal = Spacing.Screen), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun CollectionCard(collection: SavedCollection, modifier: Modifier = Modifier) {
    Surface(
        onClick = {},
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(MaterialTheme.shapes.extraSmall),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (collection.isCafe) LastEatIcons.Cup else LastEatIcons.Utensils,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Column {
                Text(collection.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text("식당 ${collection.count}곳", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
