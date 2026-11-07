package com.lasteat.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lasteat.app.data.SampleData
import com.lasteat.app.data.UserSession
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.screens.BenefitsScreen
import com.lasteat.app.ui.screens.ExploreScreen
import com.lasteat.app.ui.screens.MyScreen
import com.lasteat.app.ui.screens.SavedScreen
import kotlinx.coroutines.launch

enum class MainTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector = icon) {
    Explore("주변 탐색", LastEatIcons.Map),
    Benefits("혜택·이벤트", LastEatIcons.Ticket),
    Saved("저장", LastEatIcons.Bookmark, LastEatIcons.BookmarkFilled),
    My("마이", LastEatIcons.Person),
}

/**
 * 손님(일반 사용자) 메인: 하단 탭 4개.
 * 로그인 상태와 사장님 화면 이동은 AppRoot 가 관리하고 여기로 내려줍니다.
 *
 * Navigation 라이브러리 없이 상태로 화면을 바꿉니다 (사장님 UI 와 같은 의존성 원칙).
 * 식당 상세·검색 화면이 생기면 그때 Navigation Compose 도입을 팀과 정하세요.
 *
 * 지금은 SampleData 와 화면 내부 상태로 동작합니다. 백엔드가 붙으면
 * savedIds · session · restaurants 를 ViewModel(또는 상태 홀더)로 옮기면 됩니다.
 */
@Composable
fun LastEatApp(
    session: UserSession,
    locationName: String,
    onLocationClick: () -> Unit,
    ownerMenuTitle: String,
    onKakaoLogin: () -> Unit,
    onOwnerClick: () -> Unit,
    onResetOnboarding: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.Explore) }
    val savedIds = remember { mutableStateListOf("r1", "r2", "r3", "r4") }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val todo: (String) -> Unit = { what -> scope.launch { snackbar.showSnackbar("$what 화면은 준비 중이에요") } }

    val toggleSave: (String) -> Unit = { id ->
        if (session is UserSession.Guest) {
            scope.launch { snackbar.showSnackbar("로그인하면 식당을 저장할 수 있어요") }
        } else if (id in savedIds) savedIds.remove(id) else savedIds.add(id)
    }
    val restaurants = SampleData.restaurants
    val shownSaved = if (session is UserSession.Guest) emptySet<String>() else savedIds.toSet()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { MainBottomBar(tab) { tab = it } },
    ) { inner ->
        Box(Modifier.padding(inner)) {
            when (tab) {
                MainTab.Explore -> ExploreScreen(
                    locationName = locationName,
                    restaurants = restaurants,
                    savedIds = shownSaved,
                    onToggleSave = toggleSave,
                    onRestaurantClick = { todo("식당 상세") },
                    onSearchClick = { todo("검색") },
                    onLocationClick = onLocationClick,
                    onBenefitsClick = { tab = MainTab.Benefits },
                    onSeeAllClick = { todo("지도·목록 결과") },
                )
                MainTab.Benefits -> BenefitsScreen(
                    locationName = locationName,
                    benefits = SampleData.benefits,
                    onLocationClick = onLocationClick,
                    onBenefitClick = { todo("혜택 상세") },
                )
                MainTab.Saved -> SavedScreen(
                    session = session,
                    locationName = locationName,
                    savedRestaurants = restaurants.filter { it.id in savedIds },
                    collections = SampleData.collections,
                    onLocationClick = onLocationClick,
                    onLoginClick = { tab = MainTab.My },
                    onExploreClick = { tab = MainTab.Explore },
                    onRestaurantClick = { todo("식당 상세") },
                    onToggleSave = toggleSave,
                    onNewCollectionClick = { todo("새 컬렉션") },
                )
                MainTab.My -> MyScreen(
                    session = session,
                    locationName = locationName,
                    savedCount = shownSaved.size,
                    onLocationClick = onLocationClick,
                    onKakaoLoginClick = onKakaoLogin,
                    onSavedClick = { tab = MainTab.Saved },
                    ownerMenuTitle = ownerMenuTitle,
                    onOwnerClick = onOwnerClick,
                    onNoticeClick = { todo("공지사항") },
                    onFaqClick = { todo("자주 묻는 질문") },
                    onSettingsClick = { todo("앱 설정") },
                    onResetOnboarding = onResetOnboarding,
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(current: MainTab, onSelect: (MainTab) -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            MainTab.entries.forEach { t ->
                val selected = t == current
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(t) },
                    icon = { Icon(if (selected) t.selectedIcon else t.icon, null, Modifier.size(22.dp)) },
                    label = { Text(t.label, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
