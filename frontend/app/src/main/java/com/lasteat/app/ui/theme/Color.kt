package com.lasteat.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * LastEat 디자인 토큰 — 색상
 * 피그마 시안(주변 탐색 · 혜택 · 저장 · 마이) 기준으로 추출한 값입니다.
 * 색을 바꿀 때는 이 파일만 고치면 앱 전체(사장님 화면 포함 MaterialTheme 사용 부분)에 반영됩니다.
 */
object LastEatColors {
    // 브랜드 그린
    val Green900 = Color(0xFF19563C) // 눌림 상태
    val Green700 = Color(0xFF25744F) // 메인 버튼, 선택 칩, 로고 (primary)
    val Green500 = Color(0xFF3F8A5E) // 링크성 텍스트, 아이콘 강조
    val Green100 = Color(0xFFEAF3E8) // 혜택 배너, 선택 탭 배경
    val Green50 = Color(0xFFF6F8F4)  // 연한 카드 배경

    // 중립
    val Gray900 = Color(0xFF1B1D1C) // 제목 텍스트
    val Gray700 = Color(0xFF4A4F4C) // 본문
    val Gray500 = Color(0xFF7D827F) // 보조 텍스트 (캡션 최소 대비 확보용으로 400보다 진하게)
    val Gray300 = Color(0xFFD5D9D6) // 테두리
    val Gray200 = Color(0xFFE8EBE9) // 구분선
    val Gray100 = Color(0xFFF4F5F4) // 섹션 배경
    val White = Color(0xFFFFFFFF)

    // 기능색
    val Star = Color(0xFFE5A21A)    // 별점
    val Kakao = Color(0xFFFEE500)   // 카카오 로그인 (브랜드 가이드 고정색)
    val KakaoText = Color(0xFF191919)
    val Error = Color(0xFFD64545)
    val MapBase = Color(0xFFE9EEEA) // 지도 자리 배경
}
