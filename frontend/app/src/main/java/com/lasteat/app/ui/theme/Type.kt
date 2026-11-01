package com.lasteat.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * LastEat 디자인 토큰 — 타이포그래피
 *
 * 시안은 Pretendard 계열입니다. 폰트 파일을 res/font 에 넣으면 아래 한 줄만 바꾸세요.
 *   val LastEatFont = FontFamily(Font(R.font.pretendard_regular), Font(R.font.pretendard_bold, FontWeight.Bold), ...)
 * 사장님 화면(Owner UI)은 MaterialTheme.typography 의 fontFamily 를 그대로 상속하므로
 * 여기서 바꾸면 사장님 화면 글꼴도 함께 바뀝니다.
 */
val LastEatFont: FontFamily = FontFamily.Default

private fun style(size: Int, line: Int, weight: FontWeight) = TextStyle(
    fontFamily = LastEatFont,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = (-0.2).sp,
)

/** 시안 기준 사용처를 주석으로 적어 두었습니다. */
val LastEatTypography = Typography(
    headlineLarge = style(26, 34, FontWeight.Bold),     // 혜택 배너 큰 제목
    headlineMedium = style(22, 30, FontWeight.Bold),    // 탭 화면 제목 (저장, 마이, 혜택·이벤트)
    headlineSmall = style(20, 28, FontWeight.Bold),     // 식당 상세 이름, 혜택 카드 "마감 할인 10%"
    titleLarge = style(18, 26, FontWeight.Bold),        // 섹션 제목 (가까운 곳에서, 맛있는 발견)
    titleMedium = style(16, 24, FontWeight.Bold),       // 카드의 식당 이름
    titleSmall = style(14, 20, FontWeight.SemiBold),    // 리스트 항목, 버튼 보조
    bodyLarge = style(15, 22, FontWeight.Normal),       // 본문
    bodyMedium = style(13, 19, FontWeight.Normal),      // 카드 설명, 메타 정보
    bodySmall = style(12, 17, FontWeight.Normal),       // 캡션 (11sp 이하는 쓰지 않음)
    labelLarge = style(15, 20, FontWeight.Bold),        // 메인 버튼
    labelMedium = style(13, 18, FontWeight.SemiBold),   // 칩
    labelSmall = style(11, 14, FontWeight.SemiBold),    // 배지, 하단 탭 라벨
)
