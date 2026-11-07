package com.lasteat.app.ui.onboarding

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lasteat.app.ui.components.PrimaryButton
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.Spacing
import com.lasteat.owner.BenefitKind
import com.lasteat.owner.BenefitTerms
import com.lasteat.owner.moneyValue
import com.lasteat.owner.validateBenefitInput
import java.time.LocalDate
import java.util.Locale

// ─────────────────────────────────────────────────────────────
// 사장님 · 카카오 로그인 (필수)
// ─────────────────────────────────────────────────────────────
@Composable
fun OwnerKakaoScreen(onLogin: () -> Unit, onBack: () -> Unit) {
    OnboardingFrame(
        barLabel = "사장님 · 시작하기",
        eyebrow = "LastEat 사장님",
        title = "우리 가게의 좋은 맛을\n가까운 이웃에게",
        subtitle = "가게 등록부터 혜택 관리까지, 한곳에서 시작해요.",
        onBack = onBack,
        bottom = {
            KakaoButton("카카오로 사장님 시작하기", onLogin)
            BottomCaption("로그인 후 약관 동의와 사장님 인증을 진행해요. 이미 등록한 사장님은 관리 홈으로 이동해요.")
        },
    ) {
        StoreIllustration()
        Spacer(Modifier.height(Spacing.xl))
        InfoNote(
            icon = LastEatIcons.Shield,
            title = "사장님은 로그인이 꼭 필요해요",
            text = "가게 소유 확인과 안전한 혜택 관리를 위해 카카오 계정으로 로그인해 주세요. 공식 카카오 로그인 화면으로 연결돼요.",
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 사장님 · 내 식당 찾기 (1/3)
// ─────────────────────────────────────────────────────────────
@Composable
fun ShopSearchScreen(onPick: (ShopInfo) -> Unit, onRegister: () -> Unit, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { OnboardingSamples.searchShops(query) }

    OnboardingFrame(
        barLabel = "가게 등록 · 1 / 3",
        title = "사장님의 식당을\n찾아볼까요?",
        subtitle = "가게 이름이나 주소로 검색해 주세요.",
        onBack = onBack,
        bottom = {
            Text(
                "검색해도 우리 가게가 없나요?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = Spacing.sm),
            )
            PrimaryButton("직접 식당 등록 신청", onRegister, leadingIcon = LastEatIcons.Plus)
            BottomCaption("목록에서 식당을 선택하면 정보를 확인할 수 있어요.")
        },
    ) {
        LabeledField("식당 검색", required = false) {
            InputField(query, { query = it }, "예) 연남 다정", trailingIcon = LastEatIcons.Search)
        }
        Spacer(Modifier.height(Spacing.lg))
        if (query.isNotBlank()) {
            Text(
                if (results.isEmpty()) "검색 결과가 없어요. 직접 등록 신청을 해 주세요." else "검색 결과 ${results.size}곳",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.sm))
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            results.forEach { shop ->
                WhiteCard(onClick = { onPick(shop) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(shop.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("${shop.address}, ${shop.detailAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(Spacing.sm))
                            TagChip(shop.category)
                        }
                        Icon(LastEatIcons.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 사장님 · 식당 정보 확인 (1/3)
// ─────────────────────────────────────────────────────────────
@Composable
fun ShopConfirmScreen(shop: ShopInfo, onConfirm: () -> Unit, onSearchAgain: () -> Unit, onBack: () -> Unit) {
    OnboardingFrame(
        barLabel = "가게 등록 · 1 / 3",
        title = "우리 가게 정보가\n맞는지 확인해 주세요",
        subtitle = "선택한 식당으로 사장님 인증을 진행해요.",
        onBack = onBack,
        bottom = {
            PrimaryButton("맞아요, 사장님 인증하기", onConfirm)
            PlainTextButton("다른 식당 다시 찾기", onSearchAgain)
        },
    ) {
        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(LastEatIcons.Store, size = 48)
                Spacer(Modifier.width(Spacing.md))
                Column {
                    Text(shop.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    TagChip(shop.category)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = Spacing.md), color = OnbColors.Line)
            InfoBlock("주소", "${shop.address}\n${shop.detailAddress}")
            InfoBlock("전화번호", shop.phone)
            InfoBlock("영업시간", shop.hoursText)
        }
        Spacer(Modifier.height(Spacing.lg))
        InfoNote(LastEatIcons.Info, "가게 이름과 주소가 사업자등록증 정보와 일치하는지 확인해 주세요.")
    }
}

// ─────────────────────────────────────────────────────────────
// 사장님 · 식당 직접 등록 (검색 결과에 없을 때, 1/3)
// ─────────────────────────────────────────────────────────────
@Composable
fun ShopRegisterScreen(initial: ShopInfo?, onSave: (ShopInfo) -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var address by rememberSaveable { mutableStateOf(initial?.address ?: "") }
    var detail by rememberSaveable { mutableStateOf(initial?.detailAddress ?: "") }
    var category by rememberSaveable { mutableStateOf(initial?.category ?: "") }
    var phone by rememberSaveable { mutableStateOf(initial?.phone ?: "") }
    var hours by rememberSaveable { mutableStateOf(initial?.hoursText ?: "") }
    val valid = listOf(name, address, detail, category, phone).all { it.isNotBlank() }

    OnboardingFrame(
        barLabel = "가게 등록 · 검색 결과에 없는 경우",
        eyebrow = "직접 등록 신청 · 1 / 3",
        title = "목록에 없는 식당도\n등록할 수 있어요",
        subtitle = "가게 정보를 남겨주시면 인증 자료와 함께 확인해 드려요. * 표시는 필수 항목이에요.",
        onBack = onBack,
        bottom = {
            PrimaryButton(
                "정보 저장하고 사장님 인증하기",
                onClick = {
                    onSave(ShopInfo(null, name.trim(), address.trim(), detail.trim(), category, phone.trim(), hours.trim().ifBlank { "영업시간 미등록" }))
                },
                enabled = valid,
            )
            BottomCaption("입력한 정보는 최종 신청 전까지 공개되지 않아요.")
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            LabeledField("식당 이름") { InputField(name, { name = it }, "예) 연남 다정식탁") }
            // TODO(주소 검색): 카카오 우편번호/로컬 API 연결 시 검색 아이콘으로 주소 검색 열기
            LabeledField("가게 주소") { InputField(address, { address = it }, "도로명 주소", trailingIcon = LastEatIcons.Search) }
            LabeledField("상세 주소") { InputField(detail, { detail = it }, "예) 1층") }
            LabeledField("업종") { SelectField(category, OnboardingSamples.categories, { category = it }, "업종을 선택해 주세요") }
            LabeledField("가게 전화번호") { InputField(phone, { phone = it }, "예) 02-332-1024", keyboard = KeyboardType.Phone) }
            LabeledField("영업시간", required = false) { InputField(hours, { hours = it }, "예) 화–일 11:30–21:00 / 월요일 휴무") }
        }
        Spacer(Modifier.height(Spacing.xl))
        InfoNote(
            icon = LastEatIcons.Clipboard,
            title = "식당 등록과 사장님 인증을 한 번에",
            text = "다음 단계에서 사업자등록증을 첨부해 주세요. 혜택 등록 후 신청이 접수되며, 검토는 1~2일 걸려요.",
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 사장님 · 사업자 인증 (2/3)
// ─────────────────────────────────────────────────────────────
private const val MaxFileBytes = 10L * 1024 * 1024

private fun formatBizNumber(input: String): String {
    val d = input.filter(Char::isDigit).take(10)
    return when {
        d.length <= 3 -> d
        d.length <= 5 -> "${d.take(3)}-${d.drop(3)}"
        else -> "${d.take(3)}-${d.substring(3, 5)}-${d.drop(5)}"
    }
}

private fun formatSize(bytes: Long): String =
    if (bytes >= 1024 * 1024) String.format(Locale.KOREA, "%.1f MB", bytes / 1024.0 / 1024.0)
    else String.format(Locale.KOREA, "%d KB", (bytes / 1024).coerceAtLeast(1))

@Composable
fun BizVerifyScreen(shop: ShopInfo, initial: BizInfo?, onSave: (BizInfo) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var number by rememberSaveable { mutableStateOf(initial?.number?.let(::formatBizNumber) ?: "") }
    var ownerName by rememberSaveable { mutableStateOf(initial?.ownerName ?: "") }
    var company by rememberSaveable { mutableStateOf(initial?.companyName ?: shop.name) }
    var mobile by rememberSaveable { mutableStateOf(initial?.mobile ?: "") }
    var fileName by rememberSaveable { mutableStateOf(initial?.fileName ?: "") }
    var fileUri by rememberSaveable { mutableStateOf(initial?.fileUri ?: "") }
    var fileSize by rememberSaveable { mutableStateOf("") }
    var fileError by rememberSaveable { mutableStateOf<String?>(null) }
    var agreed by rememberSaveable { mutableStateOf(initial != null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        var name = "사업자등록증"
        var size = -1L
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                name = c.getString(0) ?: name
                if (!c.isNull(1)) size = c.getLong(1)
            }
        }
        if (size > MaxFileBytes) {
            fileError = "10MB 이하 파일만 올릴 수 있어요."
            return@rememberLauncherForActivityResult
        }
        // 나중에 업로드할 수 있도록 읽기 권한 유지 (실패해도 이번 실행 중에는 사용 가능)
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        fileError = null
        fileName = name
        fileUri = uri.toString()
        fileSize = if (size >= 0) formatSize(size) else ""
    }

    val digits = number.filter(Char::isDigit)
    val numberError = number.isNotEmpty() && digits.length != 10
    val mobileDigits = mobile.filter(Char::isDigit)
    val mobileError = mobile.isNotEmpty() && (mobileDigits.length !in 10..11 || !mobileDigits.startsWith("01"))
    val valid = digits.length == 10 && ownerName.isNotBlank() && company.isNotBlank() &&
        mobileDigits.length in 10..11 && !mobileError && fileUri.isNotBlank() && agreed

    OnboardingFrame(
        barLabel = "가게 등록 · 2 / 3",
        title = "진짜 사장님인지\n확인할게요",
        subtitle = "${shop.name}의 사업자 정보를 입력해 주세요. * 표시는 필수 항목이에요.",
        onBack = onBack,
        bottom = {
            PrimaryButton(
                "인증 정보 저장하고 혜택 등록",
                onClick = { onSave(BizInfo(digits, ownerName.trim(), company.trim(), mobile.trim(), fileName, fileUri)) },
                enabled = valid,
            )
            BottomCaption("다음 단계에서 혜택을 등록하면 신청이 완료돼요.")
        },
    ) {
        Row(
            Modifier.fillMaxWidth().background(OnbColors.Tint, MaterialTheme.shapes.small).padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(LastEatIcons.Store, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Spacing.sm))
            Text(shop.name, style = MaterialTheme.typography.titleSmall)
        }
        Spacer(Modifier.height(Spacing.xl))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            LabeledField(
                "사업자등록번호",
                help = if (numberError) "사업자등록번호 10자리를 확인해 주세요." else "사업자등록증에 기재된 10자리 번호를 입력해 주세요.",
                isError = numberError,
            ) { InputField(number, { number = formatBizNumber(it) }, "123-45-67890", keyboard = KeyboardType.Number, isError = numberError) }
            LabeledField("대표자 이름") { InputField(ownerName, { ownerName = it }, "사업자등록증의 대표자 이름") }
            LabeledField("사업자 상호") { InputField(company, { company = it }, "사업자등록증의 상호") }
            LabeledField(
                "연락받을 휴대폰 번호",
                help = if (mobileError) "휴대폰 번호를 확인해 주세요." else "인증 결과와 보완 요청을 이 번호로 안내해요.",
                isError = mobileError,
            ) { InputField(mobile, { mobile = it }, "010-1234-5678", keyboard = KeyboardType.Phone, isError = mobileError) }
            LabeledField(
                "사업자등록증 첨부",
                help = fileError ?: "JPG, PNG, PDF · 최대 10MB 대표자명, 상호, 사업장 주소가 잘 보이게 올려 주세요.",
                isError = fileError != null,
            ) {
                if (fileUri.isBlank()) {
                    Surface(
                        onClick = { picker.launch(arrayOf("image/jpeg", "image/png", "application/pdf")) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = MaterialTheme.shapes.small,
                        color = Color.White,
                        border = BorderStroke(1.dp, OnbColors.Line),
                    ) {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(LastEatIcons.Plus, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("파일 선택하기", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    WhiteCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(LastEatIcons.FileDoc, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(Spacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(fileName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                Text(
                                    listOf(fileSize, "첨부 완료").filter { it.isNotBlank() }.joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { fileUri = ""; fileName = ""; fileSize = "" }) {
                                Icon(LastEatIcons.Close, "첨부 삭제", Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.lg))
        InfoNote(LastEatIcons.Lock, "주민등록번호 등 불필요한 개인정보는 가려주세요. 서류는 사장님 인증 목적으로만 사용해요.")
        Spacer(Modifier.height(Spacing.md))
        CheckRow(agreed, "[필수] 사업자 인증 정보 수집·이용 동의", onToggle = { agreed = !agreed })
    }
}

// ─────────────────────────────────────────────────────────────
// 사장님 · 첫 혜택 등록 (3/3)
// 시안이 없던 단계라, 팀원 사장님 화면의 '새 혜택 만들기'와 같은 항목으로 구성했습니다.
// 결과(BenefitTerms)는 사장님 홈의 '우리 가게 혜택'에 '검토 중'으로 그대로 들어갑니다.
// ─────────────────────────────────────────────────────────────
@Composable
fun BenefitSetupScreen(shop: ShopInfo, onSubmit: (BenefitTerms?, Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var kind by rememberSaveable { mutableStateOf(BenefitKind.FREE_ITEM) }
    var value by rememberSaveable { mutableStateOf("") }
    var minimum by rememberSaveable { mutableStateOf("") }
    var maximum by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var expires by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    var days by rememberSaveable { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }
    var starts by rememberSaveable { mutableStateOf("00:00") }
    var ends by rememberSaveable { mutableStateOf("23:59") }

    val number = value.moneyValue() ?: -1
    val terms = BenefitTerms(
        percent = if (kind == BenefitKind.PERCENT) number else 0,
        minimum = minimum.ifBlank { "0" }.moneyValue() ?: -1,
        maximum = if (kind == BenefitKind.PERCENT) maximum.ifBlank { "0" }.moneyValue() ?: -1 else 0,
        expires = expires,
        kind = kind,
        amount = if (kind == BenefitKind.FIXED) number else 0,
        freeItem = if (kind == BenefitKind.FREE_ITEM) value.trim() else "",
        days = days,
        starts = starts,
        ends = ends,
    )
    val qty = quantity.moneyValue()
    val error = when {
        value.isBlank() -> null // 아직 입력 전
        kind == BenefitKind.PERCENT && number !in 1..100 -> "할인율은 1~100 사이로 입력해 주세요."
        else -> validateBenefitInput(terms)
    }
    val valid = value.isNotBlank() && error == null && (qty ?: 0) > 0

    OnboardingFrame(
        barLabel = "가게 등록 · 3 / 3",
        title = "이웃에게 전할\n첫 혜택을 만들어요",
        subtitle = "${shop.name}을 처음 찾는 손님에게 보여줄 혜택이에요. 승인 후 공개돼요.",
        onBack = onBack,
        bottom = {
            PrimaryButton("혜택 등록하고 신청 완료", onClick = { onSubmit(terms, qty ?: 0) }, enabled = valid)
            PlainTextButton("혜택은 나중에 등록할게요") { onSubmit(null, 0) }
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            BenefitKind.entries.forEach { k ->
                com.lasteat.app.ui.components.LastEatChip(k.label, k == kind, onClick = { kind = k; value = "" })
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            when (kind) {
                BenefitKind.PERCENT -> LabeledField("할인율 (%)") { InputField(value, { value = it }, "예) 10", keyboard = KeyboardType.Number) }
                BenefitKind.FIXED -> LabeledField("할인 금액 (원)") { InputField(value, { value = it }, "예) 2000", keyboard = KeyboardType.Number) }
                BenefitKind.FREE_ITEM -> LabeledField("무료로 제공할 메뉴") { InputField(value, { value = it }, "예) 수제 식혜 1잔") }
            }
            LabeledField("최소 주문 금액 (원)", required = false, help = "제한이 없으면 비워 두세요.") {
                InputField(minimum, { minimum = it }, "예) 20000", keyboard = KeyboardType.Number)
            }
            if (kind == BenefitKind.PERCENT) {
                LabeledField("최대 할인 금액 (원)", required = false, help = "할인 한도가 없으면 비워 두세요.") {
                    InputField(maximum, { maximum = it }, "예) 5000", keyboard = KeyboardType.Number)
                }
            }
            LabeledField("총 발급 수량 (장)") { InputField(quantity, { quantity = it }, "예) 100", keyboard = KeyboardType.Number) }
            LabeledField("쿠폰 사용 종료일") {
                Box {
                    InputField(expires, {}, "", trailingIcon = LastEatIcons.Calendar)
                    // 입력칸 위를 덮어 누르면 달력이 열리게 함
                    Surface(
                        onClick = {
                            val d = runCatching { LocalDate.parse(expires) }.getOrElse { LocalDate.now() }
                            DatePickerDialog(context, { _, y, m, day -> expires = LocalDate.of(y, m + 1, day).toString() }, d.year, d.monthValue - 1, d.dayOfMonth)
                                .apply { datePicker.minDate = System.currentTimeMillis() }
                                .show()
                        },
                        color = Color.Transparent,
                        modifier = Modifier.matchParentSize(),
                    ) {}
                }
            }
            LabeledField("사용 가능한 요일") {
                val names = listOf("월", "화", "수", "목", "금", "토", "일")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    names.forEachIndexed { i, n ->
                        val d = i + 1
                        val on = d in days
                        Surface(
                            onClick = { days = if (on) days - d else days + d },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = MaterialTheme.shapes.small,
                            color = if (on) OnbColors.Tint else Color.White,
                            border = BorderStroke(1.dp, if (on) MaterialTheme.colorScheme.primary else OnbColors.Line),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(n, style = MaterialTheme.typography.labelMedium, color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Box(Modifier.weight(1f)) { LabeledField("사용 시작 시간") { InputField(starts, { starts = it }, "00:00", trailingIcon = LastEatIcons.Clock) } }
                Box(Modifier.weight(1f)) { LabeledField("사용 종료 시간") { InputField(ends, { ends = it }, "23:59", trailingIcon = LastEatIcons.Clock) } }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(Spacing.md))
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        if (value.isNotBlank() && error == null) {
            Spacer(Modifier.height(Spacing.xl))
            Text("손님에게 이렇게 보여요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            WhiteCard {
                Text(shop.name, style = MaterialTheme.typography.titleSmall)
                Text(terms.headline(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("${terms.description()} · ${terms.scheduleLabel()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 사장님 · 신청 완료
// ─────────────────────────────────────────────────────────────
@Composable
fun OwnerCompleteScreen(application: OwnerApplication, onGoHome: () -> Unit) {
    OnboardingFrame(
        barLabel = "사장님 · 접수 완료",
        title = "신청이 잘 접수됐어요!\n조금만 기다려 주세요",
        subtitle = "${application.shop.name}의 사장님 인증과 등록한 혜택을 꼼꼼하게 확인하고 있어요.",
        onBack = null,
        bottom = {
            PrimaryButton("사장님 홈으로 가기", onGoHome)
            BottomCaption("홈에서 검토 상태와 신청 내용을 확인할 수 있어요.")
        },
    ) {
        IconTile(LastEatIcons.Check, size = 56)
        Spacer(Modifier.height(Spacing.xl))
        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("검토는 1~2일 걸려요", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TagChip("검토 중")
            }
            HorizontalDivider(Modifier.padding(vertical = Spacing.md), color = OnbColors.Line)
            SummaryLine("접수일", application.submittedAt)
            SummaryLine("신청 식당", application.shop.name)
            SummaryLine("등록 혜택", application.benefit?.headline() ?: "나중에 등록")
            SummaryLine("알림 받을 번호", application.biz.mobile)
        }
        Spacer(Modifier.height(Spacing.lg))
        InfoNote(
            icon = LastEatIcons.Bell,
            title = "검토가 끝나면 알려드릴게요",
            text = "결과는 앱 알림과 문자로 안내해요. 보완이 필요하면 연락드리며, 승인 전에는 혜택을 사용할 수 없어요.",
        )
    }
}
