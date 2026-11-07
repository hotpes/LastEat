package com.lasteat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lasteat.app.data.UserSession
import com.lasteat.app.ui.components.LastEatTopBar
import com.lasteat.app.ui.components.PrimaryButton
import com.lasteat.app.ui.components.ScreenTitle
import com.lasteat.app.ui.components.SectionDivider
import com.lasteat.app.ui.components.TintedBanner
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.LastEatColors
import com.lasteat.app.ui.theme.Spacing

@Composable
fun MyScreen(
    session: UserSession,
    locationName: String,
    savedCount: Int,
    onLocationClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onSavedClick: () -> Unit,
    ownerMenuTitle: String,
    onOwnerClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onFaqClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onResetOnboarding: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        LastEatTopBar(locationName, onLocationClick)
        ScreenTitle("마이", "나의 맛있는 일상을 시작해 보세요")

        // 프로필
        Row(
            Modifier.padding(horizontal = Spacing.Screen, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(48.dp).clip(MaterialTheme.shapes.extraLarge).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(LastEatIcons.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(Spacing.md))
            Column {
                when (session) {
                    UserSession.Guest -> {
                        Text("반가워요, 맛있는 발견의 시작", style = MaterialTheme.typography.titleMedium)
                        Text("로그인하고 나만의 맛집과 혜택을 모아보세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is UserSession.Member -> {
                        Text("${session.nickname}님, 반가워요", style = MaterialTheme.typography.titleMedium)
                        Text("저장한 식당 ${savedCount}곳", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (session is UserSession.Guest) {
            PrimaryButton(
                text = "카카오로 시작하기",
                onClick = onKakaoLoginClick,
                containerColor = LastEatColors.Kakao,
                contentColor = LastEatColors.KakaoText,
                leadingIcon = LastEatIcons.Chat,
                modifier = Modifier.padding(horizontal = Spacing.Screen),
            )
            Text(
                "카카오 계정으로 간편하게 로그인 · 회원가입해요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = Spacing.md),
            )
        }

        SectionDivider()
        SectionLabel("나의 활동")
        MenuRow(
            LastEatIcons.Bookmark, "저장한 식당 · 컬렉션",
            trailing = if (session is UserSession.Guest) "로그인 후 이용" else "${savedCount}곳",
            onClick = onSavedClick,
        )
        TintedBanner(
            eyebrow = "사장님이신가요?",
            title = ownerMenuTitle,
            description = "$locationName 이웃에게 가게의 혜택을 알려보세요",
            icon = LastEatIcons.Store,
            onClick = onOwnerClick,
            modifier = Modifier.padding(horizontal = Spacing.Screen, vertical = Spacing.sm),
        )
        Spacer(Modifier.height(Spacing.md))

        SectionDivider()
        SectionLabel("서비스 안내")
        MenuRow(LastEatIcons.Megaphone, "공지사항", onClick = onNoticeClick)
        MenuRow(LastEatIcons.Help, "자주 묻는 질문 · 문의하기", onClick = onFaqClick)
        MenuRow(LastEatIcons.Settings, "앱 설정", onClick = onSettingsClick)
        // 시연·테스트용: 처음 실행 화면(역할 선택)을 다시 봅니다. 출시 전 삭제하세요.
        MenuRow(LastEatIcons.Refresh, "처음 화면 다시 보기", onClick = onResetOnboarding)

        Column(Modifier.padding(Spacing.Screen)) {
            Row {
                Text("이용약관", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.md))
                Text("개인정보 처리방침", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(Spacing.xs))
            Text("LastEat · 버전 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Spacing.Screen, top = Spacing.lg, bottom = Spacing.xs),
    )
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, trailing: String? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Screen, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.md))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(Spacing.xs))
        }
        Icon(LastEatIcons.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
