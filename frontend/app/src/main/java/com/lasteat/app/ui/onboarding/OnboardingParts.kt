package com.lasteat.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasteat.app.ui.components.PrimaryButton
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.LastEatColors
import com.lasteat.app.ui.theme.Spacing

// 시안의 온보딩 전용 색 (메인 테마 위에 덧씀)
internal object OnbColors {
    val Canvas = Color(0xFFF6F8F4)       // 화면 배경
    val Tint = Color(0xFFE8F1E6)         // 안내 박스, 일러스트 배경, 칩
    val TintStrong = Color(0xFFC9DFC9)   // 일러스트 포인트
    val Deep = Color(0xFF3A6B49)         // 간판·문
    val Line = Color(0xFFE2E8E0)         // 카드 테두리
}

// ─────────────────────────────────────────────────────────────
// 화면 틀
// ─────────────────────────────────────────────────────────────

/** 상단 바: [<] 단계 이름 ……… LastEat */
@Composable
fun OnbTopBar(label: String, onBack: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().height(52.dp).padding(start = 4.dp, end = Spacing.Screen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(LastEatIcons.ChevronLeft, "뒤로", Modifier.size(20.dp)) }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text("LastEat", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * 온보딩 공통 화면: 상단 바 + (작은 초록 문구) + 큰 제목 + 설명 + 본문(스크롤) + 하단 버튼 영역
 */
@Composable
fun OnboardingFrame(
    barLabel: String,
    title: String,
    onBack: (() -> Unit)? = null,
    eyebrow: String? = null,
    subtitle: String? = null,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(OnbColors.Canvas)
            .systemBarsPadding()
            .imePadding(),
    ) {
        OnbTopBar(barLabel, onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.Screen),
        ) {
            Spacer(Modifier.height(Spacing.md))
            if (eyebrow != null) {
                Text(eyebrow, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(Spacing.sm))
            }
            Text(title, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, lineHeight = 36.sp))
            if (subtitle != null) {
                Spacer(Modifier.height(Spacing.sm))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(Spacing.xl))
            content()
            Spacer(Modifier.height(Spacing.lg))
        }
        Column(Modifier.padding(start = Spacing.Screen, end = Spacing.Screen, bottom = Spacing.md, top = Spacing.sm), content = bottom)
    }
}

/** 버튼 아래 작은 회색 안내 문구 */
@Composable
fun BottomCaption(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
    )
}

// ─────────────────────────────────────────────────────────────
// 버튼
// ─────────────────────────────────────────────────────────────

@Composable
fun KakaoButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        containerColor = LastEatColors.Kakao,
        contentColor = LastEatColors.KakaoText,
        leadingIcon = LastEatIcons.Chat,
    )
}

@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = MaterialTheme.shapes.small,
        color = Color.White,
        border = BorderStroke(1.dp, OnbColors.Line),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun PlainTextButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────
// 박스 · 카드
// ─────────────────────────────────────────────────────────────

/** 연한 초록 안내 박스: 아이콘 + (굵은 제목) + 설명 */
@Composable
fun InfoNote(icon: ImageVector, text: String, modifier: Modifier = Modifier, title: String? = null) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(OnbColors.Tint)
            .padding(Spacing.lg),
    ) {
        Icon(icon, null, Modifier.size(18.dp).padding(top = 1.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Spacing.md))
        Column {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
            }
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 흰 카드 */
@Composable
fun WhiteCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else OnbColors.Line)
    val color = if (selected) OnbColors.Tint else Color.White
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = color, border = border) {
            Column(Modifier.padding(Spacing.lg), content = content)
        }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = color, border = border) {
            Column(Modifier.padding(Spacing.lg), content = content)
        }
    }
}

/** 작은 초록 칩 (업종, 검토 중 등) */
@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(OnbColors.Tint)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** 둥근 사각 아이콘 상자 */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Int = 48,
    background: Color = OnbColors.Tint,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((size / 2).dp))
    }
}

// ─────────────────────────────────────────────────────────────
// 약관 체크
// ─────────────────────────────────────────────────────────────

@Composable
fun CheckBoxMark(checked: Boolean) {
    Box(
        Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (checked) MaterialTheme.colorScheme.primary else Color.White)
            .then(if (checked) Modifier else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(5.dp))),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(LastEatIcons.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun CheckRow(checked: Boolean, text: String, onToggle: () -> Unit, showChevron: Boolean = true, onOpen: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .semantics { selected = checked }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckBoxMark(checked)
        Spacer(Modifier.width(Spacing.md))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (showChevron) {
            Icon(
                LastEatIcons.ChevronRight, "$text 보기",
                Modifier.size(18.dp).clickable(enabled = onOpen != null) { onOpen?.invoke() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun AgreeAllBox(checked: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth().semantics { role = Role.Checkbox; selected = checked },
        shape = MaterialTheme.shapes.small,
        color = Color.White,
        border = BorderStroke(1.dp, OnbColors.Line),
    ) {
        Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            CheckBoxMark(checked)
            Spacer(Modifier.width(Spacing.md))
            Text("전체 동의하기", style = MaterialTheme.typography.titleSmall)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 입력
// ─────────────────────────────────────────────────────────────

@Composable
fun InputField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    trailingIcon: ImageVector? = null,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        isError = isError,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingIcon = trailingIcon?.let { { Icon(it, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = MaterialTheme.shapes.small,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            unfocusedBorderColor = OnbColors.Line,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

/** 라벨(* 필수) + 입력 + 도움말 */
@Composable
fun LabeledField(
    label: String,
    required: Boolean = true,
    help: String? = null,
    isError: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(if (required) "$label *" else label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(Spacing.sm))
        content()
        if (help != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                help,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 업종처럼 목록에서 고르는 칸 */
@Composable
fun SelectField(value: String, options: List<String>, onSelect: (String) -> Unit, placeholder: String) {
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.small,
            color = Color.White,
            border = BorderStroke(1.dp, OnbColors.Line),
        ) {
            Row(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    value.ifBlank { placeholder },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(LastEatIcons.ChevronDown, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); open = false })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 일러스트 (이미지 파일 없이 도형으로 그림)
// ─────────────────────────────────────────────────────────────

/** "동네식당" 가게 그림 */
@Composable
fun StoreIllustration(modifier: Modifier = Modifier, height: Int = 200) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(OnbColors.Tint),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 22.dp).size(26.dp).clip(CircleShape).background(OnbColors.TintStrong),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 12.dp)) {
            // 간판
            Box(
                Modifier.width(136.dp).height(34.dp).clip(RoundedCornerShape(6.dp)).background(OnbColors.Deep),
                contentAlignment = Alignment.Center,
            ) {
                Text("동네식당", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            // 차양 (3칸)
            Row(Modifier.width(164.dp).offset(y = (-4).dp), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(3) {
                    Box(
                        Modifier.width(30.dp).height(26.dp)
                            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .background(OnbColors.Deep),
                    )
                }
            }
            // 건물
            Box(Modifier.width(148.dp).height(70.dp).offset(y = (-4).dp).background(Color.White)) {
                Box(
                    Modifier.align(Alignment.CenterStart).padding(start = 14.dp).width(52.dp).height(36.dp)
                        .clip(RoundedCornerShape(3.dp)).background(OnbColors.TintStrong),
                )
                Box(
                    Modifier.align(Alignment.BottomEnd).padding(end = 22.dp).width(32.dp).height(60.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)).background(OnbColors.Deep),
                ) {
                    Box(Modifier.align(Alignment.CenterEnd).padding(end = 6.dp).size(4.dp).clip(CircleShape).background(Color.White))
                }
            }
            Box(Modifier.width(184.dp).height(3.dp).offset(y = (-4).dp).background(OnbColors.Deep))
        }
    }
}

/** 위치 안내용 동네 지도 그림 */
@Composable
fun NeighborhoodMapIllustration(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(OnbColors.Tint),
    ) {
        val w = maxWidth
        // 도로
        Box(Modifier.fillMaxWidth().height(12.dp).offset(y = 98.dp).background(Color.White.copy(alpha = 0.8f)))
        Box(Modifier.width(12.dp).fillMaxSize().offset(x = w * 0.34f).background(Color.White.copy(alpha = 0.8f)))
        Box(Modifier.width(10.dp).fillMaxSize().offset(x = w * 0.68f).background(Color.White.copy(alpha = 0.6f)))
        // 블록
        Box(Modifier.offset(18.dp, 22.dp).size(52.dp, 50.dp).clip(RoundedCornerShape(10.dp)).background(OnbColors.TintStrong))
        Box(Modifier.offset(w * 0.42f, 18.dp).size(30.dp, 62.dp).clip(RoundedCornerShape(8.dp)).background(OnbColors.TintStrong))
        Box(Modifier.offset(w * 0.73f, 10.dp).size(w * 0.24f, 80.dp).clip(RoundedCornerShape(18.dp)).background(OnbColors.TintStrong.copy(alpha = 0.7f)))
        Text("우리 동네", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.offset(28.dp, 126.dp))
        Text("경의선숲길", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.offset(w * 0.76f, 74.dp))
        // 가게 마커
        Box(
            Modifier.offset(w * 0.80f, 30.dp).size(34.dp).clip(RoundedCornerShape(9.dp)).background(Color.White),
            contentAlignment = Alignment.Center,
        ) { Icon(LastEatIcons.Utensils, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
        // 내 위치 반경 + 점
        Box(
            Modifier.align(Alignment.Center).size(104.dp).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(20.dp).clip(CircleShape).background(Color.White).padding(4.dp).clip(CircleShape).background(OnbColors.Deep))
        }
    }
}

/** 카카오 ↔ LastEat 연결 그림 */
@Composable
fun KakaoLinkIllustration() {
    WhiteCard {
        Row(
            Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(LastEatIcons.Chat, size = 56, background = LastEatColors.Kakao, tint = LastEatColors.KakaoText)
            Icon(LastEatIcons.Link, null, Modifier.padding(horizontal = Spacing.xl).size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            IconTile(LastEatIcons.Utensils, size = 56)
        }
    }
}
