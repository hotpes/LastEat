package com.lasteat.app.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import com.lasteat.app.data.AppPrefs
import com.lasteat.app.data.UserSession
import com.lasteat.app.ui.onboarding.AppRole
import com.lasteat.app.ui.onboarding.BizInfo
import com.lasteat.app.ui.onboarding.BizVerifyScreen
import com.lasteat.app.ui.onboarding.BenefitSetupScreen
import com.lasteat.app.ui.onboarding.CustomerKakaoScreen
import com.lasteat.app.ui.onboarding.CustomerStartScreen
import com.lasteat.app.ui.onboarding.LocationGuideScreen
import com.lasteat.app.ui.onboarding.NeighborhoodSelectScreen
import com.lasteat.app.ui.onboarding.OwnerApplication
import com.lasteat.app.ui.onboarding.OwnerCompleteScreen
import com.lasteat.app.ui.onboarding.OwnerKakaoScreen
import com.lasteat.app.ui.onboarding.RoleSelectScreen
import com.lasteat.app.ui.onboarding.ShopConfirmScreen
import com.lasteat.app.ui.onboarding.ShopInfo
import com.lasteat.app.ui.onboarding.ShopRegisterScreen
import com.lasteat.app.ui.onboarding.ShopSearchScreen
import com.lasteat.app.ui.onboarding.SplashScreen
import com.lasteat.app.ui.onboarding.TermsKind
import com.lasteat.app.ui.onboarding.TermsScreen
import com.lasteat.app.ui.owner.DemoOwnerHost
import com.lasteat.app.ui.owner.OwnerEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 앱 전체 화면 */
enum class AppScreen {
    Splash, RoleSelect,
    // 동네 주민
    CustomerStart, CustomerKakao, CustomerTerms, LocationGuide, Neighborhood,
    // 사장님
    OwnerKakao, OwnerTerms, ShopSearch, ShopConfirm, ShopRegister, BizVerify, BenefitSetup, OwnerComplete,
    // 홈
    CustomerHome, OwnerHome,
}

/**
 * 앱 시작 흐름 (시안 'LastEat · 서비스 흐름' 기준)
 *
 *  스플래시 → 역할 선택
 *   ├ A 동네 주민: 로그인 선택 ─┬ 카카오 로그인 → (첫 로그인) 약관 동의 ┐
 *   │                        └ 둘러보기(비로그인) ──────────────────┤
 *   │             위치 권한 안내 ─ 허용 → 주변 탐색(손님 홈)          ◀┘
 *   │                          └ 거부/직접 선택 → 동네 직접 선택 → 주변 탐색
 *   └ B 사장님: 카카오 로그인(필수) → 약관 동의 → 내 식당 찾기 ─ 선택 → 정보 확인 ┐
 *                                                 └ 없음 → 직접 등록 신청 ─┤
 *               사업자 인증 → 첫 혜택 등록 → 신청 완료 → 사장님 홈(팀원 Owner UI) ◀┘
 *
 * 화면 이동은 뒤로가기 스택(stack)으로 관리합니다. Navigation 라이브러리는 쓰지 않습니다.
 * 다음 실행부터는 스플래시 뒤 마지막으로 쓴 홈(손님 / 사장님)으로 바로 들어갑니다.
 */
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { AppPrefs(context.applicationContext) }

    // ── 기기에 저장되는 값 ─────────────────────────────
    var loggedIn by remember { mutableStateOf(prefs.loggedIn) }
    var neighborhood by remember { mutableStateOf(prefs.neighborhood) }
    var application by remember { mutableStateOf(prefs.ownerApplication) }

    // ── 사장님 신청 중 임시 값 (신청 완료 시 application 으로 저장) ──
    var draftShop by remember { mutableStateOf<ShopInfo?>(null) }
    var draftBiz by remember { mutableStateOf<BizInfo?>(null) }

    // 사장님 홈 상태: 신청 내용으로 가게 정보와 첫 혜택을 채움
    val ownerHost = remember(application) { application?.let { DemoOwnerHost.fromApplication(it) } ?: DemoOwnerHost() }

    // ── 뒤로가기 스택 ─────────────────────────────
    val stack = rememberSaveable(
        saver = listSaver<SnapshotStateList<AppScreen>, String>(
            save = { list -> list.map { it.name } },
            restore = { names -> names.map(AppScreen::valueOf).toMutableStateList() },
        ),
    ) { mutableListOf(AppScreen.Splash).toMutableStateList() }
    val top = stack.last()

    fun push(s: AppScreen) { stack.add(s) }
    fun pop() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
    fun resetTo(s: AppScreen) { stack.clear(); stack.add(s) }
    BackHandler(enabled = stack.size > 1) { pop() }

    // TODO(카카오 로그인): 카카오 SDK 로그인 → POST /auth/kakao 로 교체. 지금은 누르면 로그인된 것으로 처리.
    fun fakeKakaoLogin() {
        loggedIn = true
        prefs.loggedIn = true
    }

    fun finishCustomer(selected: String? = null) {
        selected?.let { neighborhood = it; prefs.neighborhood = it }
        prefs.role = AppRole.Customer
        resetTo(AppScreen.CustomerHome)
    }

    /** 사장님 흐름 시작: 상태에 따라 필요한 단계부터 */
    fun startOwner() {
        when {
            application != null -> { prefs.role = AppRole.Owner; resetTo(AppScreen.OwnerHome) }
            !loggedIn -> push(AppScreen.OwnerKakao)
            !prefs.ownerTermsAgreed -> push(AppScreen.OwnerTerms)
            else -> push(AppScreen.ShopSearch)
        }
    }

    val openTerm: (String) -> Unit = { name ->
        // TODO: 약관 전문 링크가 정해지면 브라우저/웹뷰로 열기
        Toast.makeText(context, "$name 전문은 준비 중이에요", Toast.LENGTH_SHORT).show()
    }

    when (top) {
        AppScreen.Splash -> SplashScreen {
            resetTo(
                when (prefs.role) {
                    AppRole.Customer -> AppScreen.CustomerHome
                    AppRole.Owner -> if (application != null) AppScreen.OwnerHome else AppScreen.RoleSelect
                    null -> AppScreen.RoleSelect
                },
            )
        }

        AppScreen.RoleSelect -> RoleSelectScreen { role ->
            when (role) {
                AppRole.Customer -> push(AppScreen.CustomerStart)
                AppRole.Owner -> startOwner()
            }
        }

        // ── 동네 주민 ─────────────────────────────
        AppScreen.CustomerStart -> CustomerStartScreen(
            onKakao = { push(AppScreen.CustomerKakao) },
            onGuest = { push(AppScreen.LocationGuide) },
            onBack = ::pop,
        )

        AppScreen.CustomerKakao -> CustomerKakaoScreen(
            onLogin = {
                fakeKakaoLogin()
                when {
                    !prefs.customerTermsAgreed -> push(AppScreen.CustomerTerms)
                    AppScreen.CustomerHome in stack -> resetTo(AppScreen.CustomerHome) // 홈의 마이 탭에서 온 경우
                    else -> push(AppScreen.LocationGuide)
                }
            },
            onGuest = {
                if (AppScreen.CustomerHome in stack) pop() else push(AppScreen.LocationGuide)
            },
            onBack = ::pop,
        )

        AppScreen.CustomerTerms -> TermsScreen(
            kind = TermsKind.Customer,
            onAgree = { marketing ->
                prefs.customerTermsAgreed = true
                prefs.marketingAgreed = marketing
                if (AppScreen.CustomerHome in stack) resetTo(AppScreen.CustomerHome) else push(AppScreen.LocationGuide)
            },
            onBack = ::pop,
            onOpenTerm = openTerm,
        )

        AppScreen.LocationGuide -> LocationGuideScreen(
            // TODO(위치): 실제 좌표 → 동네 이름 변환(역지오코딩)은 지도 SDK 결정 후 연결. 지금은 저장된 동네 사용.
            onGranted = { finishCustomer() },
            onManual = { push(AppScreen.Neighborhood) },
            onBack = ::pop,
        )

        AppScreen.Neighborhood -> {
            val fromHome = AppScreen.CustomerHome in stack
            NeighborhoodSelectScreen(
                current = neighborhood,
                fromHome = fromHome,
                onConfirm = { n ->
                    if (fromHome) {
                        neighborhood = n.name; prefs.neighborhood = n.name
                        pop()
                    } else {
                        finishCustomer(n.name)
                    }
                },
                onBack = ::pop,
            )
        }

        // ── 사장님 ─────────────────────────────
        AppScreen.OwnerKakao -> OwnerKakaoScreen(
            onLogin = {
                fakeKakaoLogin()
                if (application != null) {
                    prefs.role = AppRole.Owner; resetTo(AppScreen.OwnerHome)
                } else {
                    push(if (prefs.ownerTermsAgreed) AppScreen.ShopSearch else AppScreen.OwnerTerms)
                }
            },
            onBack = ::pop,
        )

        AppScreen.OwnerTerms -> TermsScreen(
            kind = TermsKind.Owner,
            onAgree = { marketing ->
                prefs.ownerTermsAgreed = true
                prefs.marketingAgreed = marketing
                push(AppScreen.ShopSearch)
            },
            onBack = ::pop,
            onOpenTerm = openTerm,
        )

        AppScreen.ShopSearch -> ShopSearchScreen(
            onPick = { shop -> draftShop = shop; push(AppScreen.ShopConfirm) },
            onRegister = { draftShop = null; push(AppScreen.ShopRegister) },
            onBack = ::pop,
        )

        AppScreen.ShopConfirm -> {
            val shop = draftShop
            if (shop == null) {
                LaunchedEffect(top) { pop() } // 앱이 다시 그려지며 임시 값이 사라진 경우
            } else {
                ShopConfirmScreen(
                    shop = shop,
                    onConfirm = { push(AppScreen.BizVerify) },
                    onSearchAgain = ::pop,
                    onBack = ::pop,
                )
            }
        }

        AppScreen.ShopRegister -> ShopRegisterScreen(
            initial = draftShop?.takeIf { it.id == null },
            onSave = { shop -> draftShop = shop; push(AppScreen.BizVerify) },
            onBack = ::pop,
        )

        AppScreen.BizVerify -> {
            val shop = draftShop
            if (shop == null) {
                LaunchedEffect(top) { resetTo(AppScreen.RoleSelect); startOwner() }
            } else {
                BizVerifyScreen(
                    shop = shop,
                    initial = draftBiz,
                    onSave = { biz -> draftBiz = biz; push(AppScreen.BenefitSetup) },
                    onBack = ::pop,
                )
            }
        }

        AppScreen.BenefitSetup -> {
            val shop = draftShop
            val biz = draftBiz
            if (shop == null || biz == null) {
                LaunchedEffect(top) { pop() }
            } else {
                BenefitSetupScreen(
                    shop = shop,
                    onSubmit = { terms, quantity ->
                        // TODO(백엔드): 식당 등록·사장님 인증·혜택 신청 API 로 전송 (명세에 신청 API 추가 필요)
                        val app = OwnerApplication(
                            shop = shop,
                            biz = biz,
                            benefit = terms,
                            benefitQuantity = quantity,
                            submittedAt = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        )
                        application = app
                        prefs.ownerApplication = app
                        prefs.role = AppRole.Owner
                        draftShop = null; draftBiz = null
                        resetTo(AppScreen.OwnerComplete) // 완료 화면에서는 뒤로가기로 신청 단계에 돌아가지 않음
                    },
                    onBack = ::pop,
                )
            }
        }

        AppScreen.OwnerComplete -> {
            val app = application
            if (app == null) {
                LaunchedEffect(top) { resetTo(AppScreen.RoleSelect) }
            } else {
                OwnerCompleteScreen(app, onGoHome = { resetTo(AppScreen.OwnerHome) })
            }
        }

        // ── 홈 ─────────────────────────────
        AppScreen.OwnerHome -> OwnerEntry(
            host = ownerHost,
            onSwitchToCustomer = { finishCustomer() },
        )

        AppScreen.CustomerHome -> LastEatApp(
            session = if (loggedIn) UserSession.Member(prefs.nickname, isOwner = application != null) else UserSession.Guest,
            locationName = neighborhood,
            onLocationClick = { push(AppScreen.Neighborhood) },
            ownerMenuTitle = if (application != null) "우리 가게 관리하기" else "우리 식당 혜택 등록하기",
            onKakaoLogin = { push(AppScreen.CustomerKakao) },
            onOwnerClick = { startOwner() },
            onResetOnboarding = {
                prefs.clear()
                loggedIn = false; application = null; neighborhood = prefs.neighborhood
                draftShop = null; draftBiz = null
                resetTo(AppScreen.RoleSelect)
            },
        )
    }
}
