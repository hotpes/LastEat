package com.lasteat.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LastEatColorScheme = lightColorScheme(
    primary = LastEatColors.Green700,
    onPrimary = LastEatColors.White,
    primaryContainer = LastEatColors.Green100,
    onPrimaryContainer = LastEatColors.Green900,
    secondary = LastEatColors.Green500,
    onSecondary = LastEatColors.White,
    secondaryContainer = LastEatColors.Green50,
    onSecondaryContainer = LastEatColors.Green900,
    background = LastEatColors.White,
    onBackground = LastEatColors.Gray900,
    surface = LastEatColors.White,
    onSurface = LastEatColors.Gray900,
    surfaceVariant = LastEatColors.Gray100,
    onSurfaceVariant = LastEatColors.Gray500,
    outline = LastEatColors.Gray300,
    outlineVariant = LastEatColors.Gray200,
    error = LastEatColors.Error,
    onError = LastEatColors.White,
)

/** 모서리 값: 칩 999, 입력·버튼 12, 카드 16, 큰 배너 24 */
val LastEatShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // 배지
    small = RoundedCornerShape(12.dp),       // 버튼, 입력칸, 썸네일
    medium = RoundedCornerShape(16.dp),      // 카드
    large = RoundedCornerShape(24.dp),       // 배너, 바텀시트
    extraLarge = RoundedCornerShape(999.dp), // 칩, 탭 선택 표시
)

/** 간격 토큰. 화면 좌우 여백은 Screen(20dp)으로 통일합니다. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val Screen = 20.dp
    val xl = 24.dp
    val Section = 32.dp
}

/**
 * 앱 전체를 감싸는 테마. 사장님 화면(OwnerScreen)도 반드시 이 안에서 호출합니다.
 * 다크 모드는 시안이 없어 라이트만 제공합니다. 시안이 생기면 darkColorScheme 을 추가하세요.
 */
@Composable
fun LastEatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LastEatColorScheme,
        typography = LastEatTypography,
        shapes = LastEatShapes,
        content = content,
    )
}
