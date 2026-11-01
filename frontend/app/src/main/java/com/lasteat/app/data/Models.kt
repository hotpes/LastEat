package com.lasteat.app.data

/**
 * 메인(사용자) 화면용 UI 모델.
 * 백엔드 API 스펙이 확정되면 필드명을 맞추세요. 화면은 이 모델만 보고 그립니다.
 */
data class Restaurant(
    val id: String,
    val name: String,
    val category: String,          // "한식"
    val menuSummary: String,       // "솥밥과 덮밥"
    val distanceM: Int,
    val rating: Double,
    val reviewCount: Int,
    val isOpen: Boolean,
    val photoUrl: String?,
    val benefitBadge: String? = null, // "점심특가 10% 할인" — 없으면 배지 숨김
    val collectionName: String? = null,
)

enum class BenefitKind(val label: String) {
    LastCall("마감 할인"),
    Partner("제휴 혜택"),
    Etc("기타 혜택"),
}

/** 사장님 화면의 혜택 등록(SaveBenefit) 결과가 사용자 화면에 보이는 형태 */
data class Benefit(
    val id: String,
    val restaurant: Restaurant,
    val kind: BenefitKind,
    val headline: String,     // "마감 할인 10%"
    val condition: String,    // "마감 시간대 · 2만원 이상 · 최대 5천원"
    val periodText: String,   // "2026.11.01–11.30"
)

data class SavedCollection(
    val id: String,
    val name: String,
    val count: Int,
    val isCafe: Boolean = false,
)

/** 마이 탭의 로그인 상태. 실제 값은 카카오 로그인 연동 후 앱 상태에서 내려줍니다. */
sealed interface UserSession {
    data object Guest : UserSession
    data class Member(val nickname: String, val isOwner: Boolean) : UserSession
}
