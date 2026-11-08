package com.lasteat.app.ui.owner

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

@Composable internal fun NoticeForm(notice: Notice?, onSave: (NoticeDraft) -> Unit) {
    var headline by rememberSaveable(notice?.id) { mutableStateOf(notice?.headline.orEmpty()) }
    var condition by rememberSaveable(notice?.id) { mutableStateOf(notice?.condition.orEmpty()) }
    var period by rememberSaveable(notice?.id) { mutableStateOf(notice?.periodText.orEmpty()) }
    var error by rememberSaveable(notice?.id) { mutableStateOf<String?>(null) }
    Text("할인 내용과 이용 조건을 공지로 알려주세요.", color = Muted)
    FormField("공지 제목", headline, { headline = it.take(60) }, placeholder = "할인 안내 제목을 입력해 주세요")
    FormField("할인 내용 · 이용 조건", condition, { condition = it.take(1000) }, multiline = true, placeholder = "손님에게 안내할 할인 정보와 이용 조건을 입력해 주세요")
    FormField("안내 기간", period, { period = it.take(100) }, placeholder = "기간 또는 상시 안내 여부를 입력해 주세요")
    Text("입력한 제목·내용·기간이 손님 화면의 할인 안내 카드에 표시됩니다.", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
    FormError(error)
    SaveButton(if (notice == null) "공지 게시" else "변경 내용 저장") {
        error = when {
            headline.isBlank() -> "공지 제목을 입력해 주세요."
            condition.isBlank() -> "할인 내용과 이용 조건을 입력해 주세요."
            period.isBlank() -> "안내 기간을 입력해 주세요."
            else -> null
        }
        if (error == null) onSave(NoticeDraft(headline.trim(), condition.trim(), period.trim()))
    }
}
