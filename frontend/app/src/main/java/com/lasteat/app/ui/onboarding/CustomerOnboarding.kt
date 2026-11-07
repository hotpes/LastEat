package com.lasteat.app.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lasteat.app.ui.components.PrimaryButton
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.Spacing

// ─────────────────────────────────────────────────────────────
// 주민 · 로그인 선택
// ─────────────────────────────────────────────────────────────
@Composable
fun CustomerStartScreen(onKakao: () -> Unit, onGuest: () -> Unit, onBack: () -> Unit) {
    OnboardingFrame(
        barLabel = "동네 주민 · 시작하기",
        eyebrow = "우리 동네의 맛있는 연결",
        title = "가까운 맛집,\n더 반가운 혜택",
        subtitle = "로그인하고 나만의 맛집 지도를 만들어 보세요.",
        onBack = onBack,
        bottom = {
            KakaoButton("카카오로 시작하기", onKakao)
            Spacer(Modifier.height(Spacing.sm))
            OutlineButton("일단 둘러보기 · 비로그인", onGuest)
            BottomCaption("둘러보기는 로그인 없이 주변 식당을 탐색해요. 식당 저장과 혜택 받기는 로그인이 필요해요.")
        },
    ) {
        StoreIllustration()
        Spacer(Modifier.height(Spacing.xl))
        Row(verticalAlignment = Alignment.Top) {
            Icon(LastEatIcons.Heart, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Spacing.sm))
            Text(
                "마음에 드는 식당 저장부터 혜택 받기까지, 회원이라면 모두 이용할 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 주민 · 카카오 로그인
// ─────────────────────────────────────────────────────────────
@Composable
fun CustomerKakaoScreen(onLogin: () -> Unit, onGuest: () -> Unit, onBack: () -> Unit) {
    OnboardingFrame(
        barLabel = "동네 주민 · 카카오 연결",
        title = "쓰던 카카오 계정으로\n간편하게 시작해요",
        subtitle = "복잡한 가입 없이 LastEat과 연결해요.",
        onBack = onBack,
        bottom = {
            KakaoButton("카카오로 로그인", onLogin)
            PlainTextButton("로그인하지 않고 둘러보기", onGuest)
        },
    ) {
        KakaoLinkIllustration()
        Spacer(Modifier.height(Spacing.xl))
        InfoNote(
            icon = LastEatIcons.Shield,
            title = "안전하게 연결해요",
            text = "카카오의 공식 로그인 화면으로 이동해요. LastEat은 카카오 비밀번호를 저장하거나 요청하지 않아요.",
        )
        Spacer(Modifier.height(Spacing.xl))
        Text(
            "처음 방문하셨다면 로그인 후 이용약관에 동의해 주세요. 기존 회원은 바로 다음 단계로 이동해요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 주민 · 위치 권한 안내
// ─────────────────────────────────────────────────────────────
@Composable
fun LocationGuideScreen(onGranted: () -> Unit, onManual: () -> Unit, onBack: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) onGranted() else onManual() // 거부하면 동네 직접 선택으로
    }
    OnboardingFrame(
        barLabel = "동네 주민 · 위치 설정",
        eyebrow = "회원 · 비로그인 공통",
        title = "지금 있는 동네의\n맛집을 찾아드릴게요",
        subtitle = "내 위치를 알려주시면 가까운 식당과 혜택을 더 정확하게 추천해 드려요.",
        onBack = onBack,
        bottom = {
            PrimaryButton(
                "현재 위치로 시작하기",
                onClick = { launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                leadingIcon = LastEatIcons.Locate,
            )
            Spacer(Modifier.height(Spacing.sm))
            OutlineButton("직접 동네 선택하기", onManual)
            BottomCaption("위치 권한을 거부하면 동네 선택으로 이동해요.")
        },
    ) {
        NeighborhoodMapIllustration()
        Spacer(Modifier.height(Spacing.xl))
        InfoNote(
            icon = LastEatIcons.Pin,
            title = "위치 정보는 이렇게 사용해요",
            text = "앱을 사용하는 동안 주변 식당과 거리를 안내해요. 허용하지 않아도 동네를 직접 선택할 수 있어요.",
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 주민 · 동네 직접 선택 (홈의 동네 변경에서도 같이 씀)
// ─────────────────────────────────────────────────────────────
@Composable
fun NeighborhoodSelectScreen(
    current: String?,
    onConfirm: (Neighborhood) -> Unit,
    onBack: () -> Unit,
    fromHome: Boolean = false,
) {
    var query by rememberSaveable { mutableStateOf(current?.removeSuffix("동") ?: "") }
    var selectedName by rememberSaveable { mutableStateOf(current) }
    val results = remember(query) { OnboardingSamples.searchNeighborhoods(query) }
    val selected = OnboardingSamples.neighborhoods.firstOrNull { it.name == selectedName }

    OnboardingFrame(
        barLabel = if (fromHome) "동네 변경" else "동네 주민 · 위치 거부 시",
        title = "어느 동네에서\n맛있는 한 끼를 찾나요?",
        subtitle = "동 이름이나 도로명으로 검색해 주세요.",
        onBack = onBack,
        bottom = {
            PrimaryButton(
                if (selected != null) "${selected.name}에서 둘러보기" else "동네를 선택해 주세요",
                onClick = { selected?.let(onConfirm) },
                enabled = selected != null,
            )
            BottomCaption("선택한 동네는 홈에서 언제든 바꿀 수 있어요.")
        },
    ) {
        if (!fromHome) {
            InfoNote(LastEatIcons.LocationOff, "위치 권한이 꺼져 있어요. 동네를 직접 선택해도 주변 식당을 둘러볼 수 있어요.")
            Spacer(Modifier.height(Spacing.xl))
        }
        LabeledField("동네 검색", required = false) {
            InputField(query, { query = it }, "예) 연남동, 성미산로", trailingIcon = LastEatIcons.Search)
        }
        Spacer(Modifier.height(Spacing.lg))
        if (query.isNotBlank()) {
            Text(
                if (results.isEmpty()) "검색 결과가 없어요. 다른 이름으로 찾아보세요." else "검색 결과 ${results.size}개",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.sm))
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            results.forEach { n ->
                val isSel = n.name == selectedName
                WhiteCard(selected = isSel, onClick = { selectedName = n.name }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(LastEatIcons.Pin, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(Spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(n.name, style = MaterialTheme.typography.titleMedium)
                            Text(n.region, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSel) Icon(LastEatIcons.Check, "선택됨", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Spacer(Modifier.padding(bottom = Spacing.sm))
    }
}
