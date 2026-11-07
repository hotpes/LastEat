package com.lasteat.app.data

import android.content.Context
import androidx.core.content.edit
import com.lasteat.app.ui.onboarding.AppRole
import com.lasteat.app.ui.onboarding.OwnerApplication
import com.lasteat.app.ui.onboarding.ownerApplicationFromJson
import com.lasteat.app.ui.onboarding.toJson

/**
 * 처음 실행 선택값을 기기에 저장합니다. (추가 라이브러리 없이 SharedPreferences 사용)
 *
 * 카카오 로그인·백엔드가 붙으면:
 * - loggedIn / 약관 동의 여부 → POST /auth/kakao 응답(newMember)과 토큰 저장소로 대체
 * - ownerApplication → 사장님 신청 API 응답과 GET /members/me 로 대체
 */
class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("lasteat_app", Context.MODE_PRIVATE)

    /** 마지막으로 사용한 모드. 다음 실행 때 이 홈으로 바로 들어갑니다. */
    var role: AppRole?
        get() = prefs.getString(KEY_ROLE, null)?.let { runCatching { AppRole.valueOf(it) }.getOrNull() }
        set(value) = prefs.edit { putString(KEY_ROLE, value?.name) }

    var loggedIn: Boolean
        get() = prefs.getBoolean(KEY_LOGGED_IN, false)
        set(value) = prefs.edit { putBoolean(KEY_LOGGED_IN, value) }

    var nickname: String
        get() = prefs.getString(KEY_NICKNAME, null) ?: "연남동 미식가"
        set(value) = prefs.edit { putString(KEY_NICKNAME, value) }

    var customerTermsAgreed: Boolean
        get() = prefs.getBoolean(KEY_TERMS_CUSTOMER, false)
        set(value) = prefs.edit { putBoolean(KEY_TERMS_CUSTOMER, value) }

    var ownerTermsAgreed: Boolean
        get() = prefs.getBoolean(KEY_TERMS_OWNER, false)
        set(value) = prefs.edit { putBoolean(KEY_TERMS_OWNER, value) }

    var marketingAgreed: Boolean
        get() = prefs.getBoolean(KEY_MARKETING, false)
        set(value) = prefs.edit { putBoolean(KEY_MARKETING, value) }

    /** 손님 홈에 보여줄 동네 */
    var neighborhood: String
        get() = prefs.getString(KEY_NEIGHBORHOOD, null) ?: "연남동"
        set(value) = prefs.edit { putString(KEY_NEIGHBORHOOD, value) }

    /** 사장님 신청 내용 (식당 + 사업자 인증 + 첫 혜택). 있으면 사장님 홈으로 들어갈 수 있음 */
    var ownerApplication: OwnerApplication?
        get() = prefs.getString(KEY_OWNER_APP, null)?.let(::ownerApplicationFromJson)
        set(value) = prefs.edit { if (value == null) remove(KEY_OWNER_APP) else putString(KEY_OWNER_APP, value.toJson()) }

    fun clear() = prefs.edit { clear() }

    private companion object {
        const val KEY_ROLE = "role"
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_NICKNAME = "nickname"
        const val KEY_TERMS_CUSTOMER = "terms_customer"
        const val KEY_TERMS_OWNER = "terms_owner"
        const val KEY_MARKETING = "marketing"
        const val KEY_NEIGHBORHOOD = "neighborhood"
        const val KEY_OWNER_APP = "owner_application"
    }
}
