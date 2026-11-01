package com.lasteat.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.lasteat.app.ui.owner.OwnerEntry
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
 * 메인 앱 뼈대: 하단 탭 4개 + 사장님 화면 진입.
 *
 * Navigation 라이브러리 없이 상태로 화면을 바꿉니다 (사장님 UI 와 같은 의존성 원칙).
 * 식당 상세·검색 화면이 생기면 그때 Navigation Compose 도입을 팀과 정하세요.
 *
 * 지금은 SampleData 와 화면 내부 상태로 동작합니다. 백엔드가 붙으면
 * savedIds · session · restaurants 를 ViewModel(또는 상태 홀더)로 옮기면 됩니다.
 */
@Composable
fun LastEatApp() {
    var tab by rememberSaveable { mutableStateOf(MainTab.Explore) }
    var ownerOpen by rememberSaveable { mutableStateOf(false) }
    var session by remember { mutableStateOf<UserSession>(UserSession.Guest) }
    val savedIds = remember { mutableStateListOf("r1", "r2", "r3", "r4") }
    val locationName = "연남동"

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val todo: (String) -> Unit = { what -> scope.launch { snackbar.showSnackbar("$what 화면은 준비 중이에요") } }

    if (ownerOpen) {
        // 사장님 화면은 하단 탭 없이 전체 화면으로 엽니다.
        Box(Modifier.systemBarsPadding()) {
            OwnerEntry(onClose = { ownerOpen = false })
        }
        return
    }

    val toggleSave: (String) -> Unit = { id ->
        if (session is UserSession.Guest) {
            scope.launch { snackbar.showSnackbar("로그인하면 식당을 저장할 수 있어요") }
        } else if (id in savedIds) savedIds.remove(id) else savedIds.add(id)
    }
    // TODO(로그인): 카카오 SDK 연동 전까지는 버튼을 누르면 임시 회원으로 바뀝니다.
    val fakeLogin = { session = UserSession.Member(nickname = "연남동 미식가", isOwner = false) }
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
                    onLocationClick = { todo("동네 선택") },
                    onBenefitsClick = { tab = MainTab.Benefits },
                    onSeeAllClick = { todo("지도·목록 결과") },
                )
                MainTab.Benefits -> BenefitsScreen(
                    locationName = locationName,
                    benefits = SampleData.benefits,
                    onLocationClick = { todo("동네 선택") },
                    onBenefitClick = { todo("혜택 상세") },
                )
                MainTab.Saved -> SavedScreen(
                    session = session,
                    locationName = locationName,
                    savedRestaurants = restaurants.filter { it.id in savedIds },
                    collections = SampleData.collections,
                    onLocationClick = { todo("동네 선택") },
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
                    onLocationClick = { todo("동네 선택") },
                    onKakaoLoginClick = fakeLogin,
                    onSavedClick = { tab = MainTab.Saved },
                    onOwnerClick = { ownerOpen = true },
                    onNoticeClick = { todo("공지사항") },
                    onFaqClick = { todo("자주 묻는 질문") },
                    onSettingsClick = { todo("앱 설정") },
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
