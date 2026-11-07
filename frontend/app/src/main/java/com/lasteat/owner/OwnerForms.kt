package com.lasteat.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

internal val LocalOwnerSubmitting = staticCompositionLocalOf { false }

@Composable internal fun FormField(label: String, value: String, onChange: (String) -> Unit, numeric: Boolean = false, multiline: Boolean = false, helper: String? = null, placeholder: String? = null) {
    OutlinedTextField(value, onChange, label = { Text(label) }, placeholder = if (placeholder == null) null else ({ Text(placeholder, color = Muted) }), modifier = Modifier.testTag("ownerField:$label").fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(12.dp), singleLine = !multiline, minLines = if (multiline) 3 else 1, keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text), supportingText = if (helper == null) null else ({ Text(helper) }))
}
@Composable internal fun FormError(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).semantics { liveRegion = LiveRegionMode.Assertive })
}
@Composable internal fun SaveButton(label: String = "변경 내용 저장", onClick: () -> Unit) {
    Button(onClick, enabled = !LocalOwnerSubmitting.current, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 52.dp), shape = RoundedCornerShape(13.dp)) { Text(label) }
}
@Composable internal fun ShopForm(shop: Shop, onSave: (Shop) -> Unit) {
    var name by rememberSaveable { mutableStateOf(shop.name) }
    var introduction by rememberSaveable { mutableStateOf(shop.introduction) }
    var address by rememberSaveable { mutableStateOf(shop.address) }
    var phone by rememberSaveable { mutableStateOf(shop.phone) }
    var photo by rememberSaveable { mutableStateOf(shop.photoUrl) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val picker = LocalOwnerPickImage.current
    val choosePhoto: () -> Unit = { picker?.invoke(photo) { photo = it } }
    ShopPhoto(photo, Modifier.fillMaxWidth().height(150.dp))
    OutlinedButton(choosePhoto, enabled = picker != null && !LocalOwnerSubmitting.current, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Icon(OwnerIcons.AddPhotoAlternate, null, Modifier.size(18.dp)); Text("대표 사진 변경", Modifier.padding(start = 7.dp)) }
    FormField("가게 이름", name, { name = it.take(30) })
    FormField("가게 소개", introduction, { introduction = it.take(200) }, multiline = true)
    FormField("주소", address, { address = it.take(120) })
    FormField("전화번호", phone, { phone = it.take(20) })
    FormError(error)
    SaveButton {
        error = when {
            name.isBlank() -> "가게 이름을 입력해 주세요."
            address.isBlank() -> "손님이 찾아올 수 있도록 주소를 확인해 주세요."
            phone.isBlank() -> "연락 가능한 전화번호를 확인해 주세요."
            else -> null
        }
        if (error == null) onSave(shop.copy(name = name.trim(), introduction = introduction.trim(), address = address.trim(), phone = phone.trim(), photoUrl = photo))
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun DayPicker(label: String, selected: Set<Int>, onChange: (Set<Int>) -> Unit) {
    Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 17.dp, bottom = 5.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("월", "화", "수", "목", "금", "토", "일").forEachIndexed { index, day ->
            FilterChip(selected = index + 1 in selected, onClick = { onChange(if (index + 1 in selected) selected - (index + 1) else selected + (index + 1)) }, label = { Text(day) })
        }
    }
}
@Composable internal fun HoursForm(shop: Shop, onSave: (Shop) -> Unit) {
    var opens by rememberSaveable { mutableStateOf(shop.opens) }
    var closes by rememberSaveable { mutableStateOf(shop.closes) }
    var days by rememberSaveable { mutableStateOf(shop.closedDays.toList()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) { FormField("영업 시작", opens, { opens = it.take(5) }, placeholder = "예: 11:00") }
        Box(Modifier.weight(1f)) { FormField("영업 종료", closes, { closes = it.take(5) }, placeholder = "예: 21:00") }
    }
    Text("24시간 형식으로 입력해 주세요. 종료 시간이 더 이르면 다음날까지 영업해요.", style = MaterialTheme.typography.bodySmall, color = Muted, modifier = Modifier.padding(top = 8.dp))
    DayPicker("정기 휴무일", days.toSet()) { days = it.sorted() }
    if (days.isEmpty()) Text("정기 휴무일이 있으면 선택해 주세요.", color = Muted, style = MaterialTheme.typography.bodySmall)
    FormError(error)
    SaveButton {
        error = validateHours(opens, closes)
        if (error == null) onSave(shop.copy(opens = opens, closes = closes, closedDays = days.toSet()))
    }
}
@Composable internal fun MenuManager(state: OwnerState, onSave: (MenuItem) -> Unit, onToggle: (String, Boolean) -> Unit, onDelete: (String) -> Unit) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    if (editingId != null || adding) {
        TextButton(onClick = { editingId = null; adding = false }) { Icon(OwnerIcons.ArrowBack, null, Modifier.size(17.dp)); Text("메뉴 목록", Modifier.padding(start = 5.dp)) }
        MenuForm(state.menus.find { it.id == editingId }, onSave = { onSave(it) }, onDelete = { editingId?.let(onDelete) })
    } else {
        state.menus.forEach { menu ->
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                MenuPhoto(menu.photoUrl, Modifier.size(64.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(menu.name, style = MaterialTheme.typography.titleSmall)
                    Text(menu.price.won(), color = Forest, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { editingId = menu.id }, contentPadding = PaddingValues(0.dp)) { Text("정보 수정", style = MaterialTheme.typography.labelMedium) }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(checked = menu.soldOut, onCheckedChange = { onToggle(menu.id, it) }, enabled = !LocalOwnerSubmitting.current, modifier = Modifier.semantics { contentDescription = "${menu.name} 품절" })
                    Text(if (menu.soldOut) "품절" else "판매 중", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            HorizontalDivider(color = Line)
        }
        if (state.menus.isEmpty()) Text("등록된 메뉴가 없어요. 첫 메뉴를 추가해 보세요.", color = Muted)
        SaveButton("+ 새 메뉴 추가") { adding = true }
    }
}
@Composable private fun MenuForm(menu: MenuItem?, onSave: (MenuItem) -> Unit, onDelete: () -> Unit) {
    var name by rememberSaveable(menu?.id) { mutableStateOf(menu?.name ?: "") }
    var price by rememberSaveable(menu?.id) { mutableStateOf(menu?.price?.toString() ?: "") }
    var description by rememberSaveable(menu?.id) { mutableStateOf(menu?.description ?: "") }
    var photo by rememberSaveable(menu?.id) { mutableStateOf(menu?.photoUrl ?: "") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    val picker = LocalOwnerPickImage.current
    val choosePhoto: () -> Unit = { picker?.invoke(photo) { photo = it } }
    Row(verticalAlignment = Alignment.CenterVertically) {
        MenuPhoto(photo, Modifier.size(84.dp))
        TextButton(choosePhoto, enabled = picker != null && !LocalOwnerSubmitting.current) { Icon(OwnerIcons.AddPhotoAlternate, null, Modifier.size(18.dp)); Text("사진 변경", Modifier.padding(start = 5.dp)) }
    }
    FormField("메뉴 이름", name, { name = it.take(40) })
    FormField("가격 (원)", price, { price = it.filter(Char::isDigit).take(8) }, numeric = true, placeholder = "예: 15000")
    FormField("메뉴 설명", description, { description = it.take(150) }, multiline = true)
    FormError(error)
    SaveButton("메뉴 저장") {
        val amount = price.moneyValue()
        error = when { name.isBlank() -> "메뉴 이름을 입력해 주세요."; amount == null -> "가격을 숫자로 입력해 주세요."; else -> null }
        if (error == null) onSave(MenuItem(menu?.id ?: "", name.trim(), amount!!, description.trim(), menu?.soldOut ?: false, photo))
    }
    if (menu != null) TextButton(onClick = { deleting = true }, modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) { Text("이 메뉴 삭제", color = MaterialTheme.colorScheme.error) }
    if (deleting) AlertDialog(onDismissRequest = { deleting = false }, title = { Text("메뉴를 삭제할까요?") }, text = { Text("${menu?.name} 메뉴가 손님 화면에서 사라집니다.") }, confirmButton = { TextButton(onClick = { deleting = false; onDelete() }) { Text("삭제") } }, dismissButton = { TextButton(onClick = { deleting = false }) { Text("취소") } })
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable internal fun BenefitForm(offer: Offer?, onSave: (Offer) -> Unit) {
    val original = offer?.terms
    var kind by rememberSaveable(offer?.id) { mutableStateOf(original?.kind ?: BenefitKind.PERCENT) }
    var percent by rememberSaveable(offer?.id) { mutableStateOf(original?.percent?.toString().orEmpty()) }
    var amount by rememberSaveable(offer?.id) { mutableStateOf(original?.amount?.toString().orEmpty()) }
    var freeItem by rememberSaveable(offer?.id) { mutableStateOf(original?.freeItem.orEmpty()) }
    var minimum by rememberSaveable(offer?.id) { mutableStateOf(original?.minimum?.toString().orEmpty()) }
    var maximum by rememberSaveable(offer?.id) { mutableStateOf(original?.maximum?.toString().orEmpty()) }
    var quantity by rememberSaveable(offer?.id) { mutableStateOf(offer?.quantity?.toString().orEmpty()) }
    var expires by rememberSaveable(offer?.id) { mutableStateOf(original?.expires.orEmpty()) }
    var days by rememberSaveable(offer?.id) { mutableStateOf(original?.days?.toList().orEmpty()) }
    var starts by rememberSaveable(offer?.id) { mutableStateOf(original?.starts.orEmpty()) }
    var ends by rememberSaveable(offer?.id) { mutableStateOf(original?.ends.orEmpty()) }
    var showDate by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { BenefitKind.entries.forEach { type -> FilterChip(selected = kind == type, onClick = { kind = type }, label = { Text(type.label) }) } }
    when (kind) {
        BenefitKind.PERCENT -> FormField("할인율 (%)", percent, { percent = it.filter(Char::isDigit).take(3) }, numeric = true, placeholder = "예: 10")
        BenefitKind.FIXED -> FormField("할인 금액 (원)", amount, { amount = it.filter(Char::isDigit).take(8) }, numeric = true, placeholder = "예: 3000")
        BenefitKind.FREE_ITEM -> FormField("무료로 제공할 메뉴", freeItem, { freeItem = it.take(35) }, placeholder = "제공할 메뉴를 입력해 주세요")
    }
    FormField("최소 주문 금액 (원)", minimum, { minimum = it.filter(Char::isDigit).take(8) }, numeric = true, helper = "제한이 없으면 0을 입력해 주세요.", placeholder = "예: 20000")
    if (kind == BenefitKind.PERCENT) FormField("최대 할인 금액 (원)", maximum, { maximum = it.filter(Char::isDigit).take(8) }, numeric = true, helper = "할인 한도가 없으면 0을 입력해 주세요.", placeholder = "예: 5000")
    FormField("총 발급 수량 (장)", quantity, { quantity = it.filter(Char::isDigit).take(5) }, numeric = true, placeholder = "예: 100")
    OutlinedTextField(value = expires, onValueChange = { expires = it.filter { char -> char.isDigit() || char == '-' }.take(10) }, label = { Text("쿠폰 사용 종료일") }, placeholder = { Text("YYYY-MM-DD", color = Muted) }, singleLine = true, modifier = Modifier.testTag("ownerField:쿠폰 사용 종료일").fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(12.dp), trailingIcon = { IconButton(onClick = { showDate = true }) { Icon(OwnerIcons.CalendarMonth, "혜택 종료일 선택") } })
    DayPicker("사용 가능한 요일", days.toSet()) { days = it.sorted() }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) { FormField("사용 시작 시간", starts, { starts = it.take(5) }, placeholder = "예: 00:00") }
        Box(Modifier.weight(1f)) { FormField("사용 종료 시간", ends, { ends = it.take(5) }, placeholder = "예: 23:59") }
    }
    Text("하루 종일 사용: 00:00–23:59", style = MaterialTheme.typography.bodySmall, color = Muted, modifier = Modifier.padding(top = 6.dp))
    FormError(error)
    SaveButton(if (offer == null) "혜택 만들기" else "혜택 수정 저장") {
        val terms = BenefitTerms(
            percent = if (kind == BenefitKind.PERCENT) percent.toIntOrNull() ?: -1 else 0,
            minimum = minimum.moneyValue() ?: -1,
            maximum = if (kind == BenefitKind.PERCENT) maximum.moneyValue() ?: -1 else 0,
            expires = expires, kind = kind,
            amount = if (kind == BenefitKind.FIXED) amount.moneyValue() ?: -1 else 0,
            freeItem = freeItem.trim(), days = days.toSet(), starts = starts, ends = ends,
        )
        val count = quantity.toIntOrNull()
        error = validateBenefitInput(terms) ?: if (count == null) "발급 수량을 숫자로 입력해 주세요." else null
        if (error == null) onSave(Offer(offer?.id ?: "", terms.headline(), terms, count!!, offer?.issued ?: 0, offer?.used ?: 0, offer?.paused ?: false))
    }
    if (showDate) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = runCatching { LocalDate.parse(expires).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull())
        DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = { TextButton(onClick = { dateState.selectedDateMillis?.let { expires = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }; showDate = false }, enabled = dateState.selectedDateMillis != null) { Text("선택") } }, dismissButton = { TextButton(onClick = { showDate = false }) { Text("취소") } }) { DatePicker(dateState) }
    }
}
