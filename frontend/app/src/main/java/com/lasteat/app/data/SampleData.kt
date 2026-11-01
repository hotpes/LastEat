package com.lasteat.app.data

/**
 * 화면 확인용 샘플 데이터 (시안과 같은 내용).
 * 백엔드 연동 후에는 Repository 결과로 교체하고 이 파일은 @Preview 에서만 쓰세요.
 */
object SampleData {
    val restaurants = listOf(
        Restaurant("r1", "연남 온기", "한식", "솥밥과 덮밥", 240, 4.8, 328, true, null, "점심특가 10% 할인", "든든한 한 끼"),
        Restaurant("r2", "카페 결", "카페", "커피와 디저트", 320, 4.9, 186, true, null, null, "커피 한 잔"),
        Restaurant("r3", "소담국수", "한식", "잔치국수와 비빔국수", 410, 4.6, 214, true, null, "점심 세트 2,000원 할인", "든든한 한 끼"),
        Restaurant("r4", "초록식탁", "양식", "샐러드와 파스타", 180, 4.7, 152, true, null, null, "든든한 한 끼"),
    )

    val benefits = listOf(
        Benefit(
            "b1", restaurants[0], BenefitKind.LastCall, "마감 할인 10%",
            "마감 시간대 · 2만원 이상 · 최대 5천원", "2026.11.01–11.30",
        ),
        Benefit(
            "b2", restaurants[3], BenefitKind.Partner, "아메리카노 1잔 무료",
            "메인 메뉴 2개 이상 주문 · 테이블당 1잔", "2026.11.01–11.30",
        ),
    )

    val collections = listOf(
        SavedCollection("c1", "든든한 한 끼", 3),
        SavedCollection("c2", "분위기 좋은 카페", 1, isCafe = true),
    )
}
