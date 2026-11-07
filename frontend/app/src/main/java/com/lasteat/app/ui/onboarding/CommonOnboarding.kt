package com.lasteat.app.ui.onboarding

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasteat.app.ui.components.PrimaryButton
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.Spacing
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────
// 공통 · 스플래시
// ─────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        // 실제 앱에서는 이 시간 동안 로그인 토큰 확인(/auth/refresh) 등을 합니다.
        delay(1600)
        finish()
    }
    val transition = rememberInfiniteTransition(label = "dots")
    val phase by transition.animateFloat(0f, 3f, infiniteRepeatable(tween(1200), RepeatMode.Restart), label = "phase")

    Column(
        Modifier.fillMaxSize().background(OnbColors.Canvas).systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.8f))
        Text("LastEat", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(Spacing.sm))
        Text("오늘의 한 끼, 우리 동네에서 만나요.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.Section))
        StoreIllustration(Modifier.padding(horizontal = 48.dp), height = 200)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                Box(
                    Modifier.size(6.dp)
                        .alpha(if (phase.toInt() == i) 1f else 0.3f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
        Spacer(Modifier.height(Spacing.md))
        Text("맛있는 이웃을 만나는 중", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.xl))
        HorizontalDivider(color = OnbColors.Line)
        Text(
            "GOOD FOOD, GOOD NEIGHBORS",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = Spacing.lg),
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 공통 · 역할 선택
// ─────────────────────────────────────────────────────────────
@Composable
fun RoleSelectScreen(onSelect: (AppRole) -> Unit) {
    Column(Modifier.fillMaxSize().background(OnbColors.Canvas).systemBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(52.dp).padding(horizontal = Spacing.Screen),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("함께 시작해요", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("LastEat", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f).padding(horizontal = Spacing.Screen)) {
            Spacer(Modifier.height(Spacing.md))
            Text("반가워요!\n어떻게 오셨나요?", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, lineHeight = 36.sp))
            Spacer(Modifier.height(Spacing.sm))
            Text("나에게 맞는 LastEat을 시작해 보세요.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.xl))
            RoleCard(
                icon = LastEatIcons.Utensils,
                title = "동네 주민이에요",
                description = "가까운 맛집을 발견하고\n우리 동네만의 혜택을 즐겨요.",
                highlighted = true,
                onClick = { onSelect(AppRole.Customer) },
            )
            Spacer(Modifier.height(Spacing.md))
            RoleCard(
                icon = LastEatIcons.Store,
                title = "사장님이에요",
                description = "우리 가게를 알리고\n이웃에게 특별한 혜택을 전해요.",
                highlighted = false,
                onClick = { onSelect(AppRole.Owner) },
            )
        }
        Text(
            "역할은 나중에 마이페이지에서 변경할 수 있어요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
        )
    }
}

@Composable
private fun RoleCard(icon: ImageVector, title: String, description: String, highlighted: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (highlighted) OnbColors.Tint else Color.White,
        border = BorderStroke(1.dp, OnbColors.Line),
    ) {
        Column(Modifier.padding(Spacing.xl)) {
            Row(verticalAlignment = Alignment.Top) {
                IconTile(icon, size = 44, background = if (highlighted) Color.White else OnbColors.Tint)
                Spacer(Modifier.weight(1f))
                Icon(LastEatIcons.ArrowUpRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(Spacing.xl))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.sm))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 공통 · 약관 동의 (주민 / 사장님)
// ─────────────────────────────────────────────────────────────
enum class TermsKind { Customer, Owner }

@Composable
fun TermsScreen(kind: TermsKind, onAgree: (marketing: Boolean) -> Unit, onBack: () -> Unit, onOpenTerm: (String) -> Unit = {}) {
    val items = when (kind) {
        TermsKind.Customer -> listOf("[필수] 서비스 이용약관", "[필수] 개인정보 수집 및 이용 동의", "[필수] 위치기반 서비스 이용약관", "[필수] 만 14세 이상입니다")
        TermsKind.Owner -> listOf("[필수] 서비스 이용약관", "[필수] 개인정보 수집 및 이용 동의", "[필수] 사장님 서비스 이용약관", "[필수] 만 14세 이상입니다")
    }
    var checks by rememberSaveable { mutableStateOf(List(items.size) { false }) }
    var marketing by rememberSaveable { mutableStateOf(false) }
    val allRequired = checks.all { it }
    val all = allRequired && marketing

    OnboardingFrame(
        barLabel = if (kind == TermsKind.Customer) "동네 주민 · 첫 로그인" else "사장님 · 서비스 이용 동의",
        eyebrow = "카카오 연결 완료",
        title = if (kind == TermsKind.Customer) "만나서 반가워요!\n약관을 확인해 주세요" else "사장님, 시작 전에\n약관을 확인해 주세요",
        subtitle = if (kind == TermsKind.Customer) "안심하고 이용할 수 있도록 꼭 필요한 동의예요." else "가게와 혜택을 안전하게 관리하기 위한 동의예요.",
        onBack = onBack,
        bottom = {
            PrimaryButton(
                if (kind == TermsKind.Customer) "필수 약관 동의하고 시작하기" else "동의하고 내 식당 찾기",
                onClick = { onAgree(marketing) },
                enabled = allRequired,
            )
            BottomCaption(
                (if (allRequired) "필수 항목 ${items.size}개 동의 완료" else "필수 항목 ${checks.count { it }}/${items.size}개 동의") +
                    " · 마케팅 수신 " + (if (marketing) "동의" else "미동의"),
            )
        },
    ) {
        AgreeAllBox(all) {
            val v = !all
            checks = List(items.size) { v }
            marketing = v
        }
        Spacer(Modifier.height(Spacing.sm))
        Text("선택 항목에 동의하지 않아도 서비스를 이용할 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.sm))
        items.forEachIndexed { i, label ->
            CheckRow(
                checked = checks[i],
                text = label,
                onToggle = { checks = checks.toMutableList().also { it[i] = !it[i] } },
                showChevron = !label.contains("14세"),
                onOpen = { onOpenTerm(label) },
            )
        }
        HorizontalDivider(Modifier.padding(vertical = Spacing.sm), color = OnbColors.Line)
        CheckRow(marketing, "[선택] 마케팅 정보 수신 동의", onToggle = { marketing = !marketing }, onOpen = { onOpenTerm("마케팅 정보 수신 동의") })
        Text("이벤트와 동네 혜택 소식을 알림으로 보내드려요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 카드 안 "라벨 / 값" 한 줄 (식당 정보 확인, 신청 완료 요약) */
@Composable
fun InfoBlock(label: String, value: String) {
    Column(Modifier.padding(vertical = Spacing.sm)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(96.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}
