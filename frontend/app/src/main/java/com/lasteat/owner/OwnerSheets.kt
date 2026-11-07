package com.lasteat.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable internal fun RedeemPanel(couponState: CouponUiState, onAction: (OwnerAction) -> Unit, onDone: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    var total by rememberSaveable { mutableStateOf("") }
    var inputError by rememberSaveable { mutableStateOf<String?>(null) }
    if (couponState is CouponUiState.Redeemed) {
        Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(OwnerIcons.CheckCircle, null, Modifier.size(65.dp), tint = Forest)
            Text(couponState.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 15.dp))
            Text(couponState.message, modifier = Modifier.padding(top = 12.dp))
        }
        SaveButton("확인", onDone)
        return
    }
    if (couponState !is CouponUiState.Ready) {
        Text("손님이 보여주신 쿠폰 번호를 입력해 주세요.", color = Muted)
        FormField("쿠폰 번호", code, { code = normalizeCode(it).filter(Char::isLetterOrDigit).take(32); inputError = null })
    }
    if (couponState !is CouponUiState.Ready) {
        FormError((couponState as? CouponUiState.Rejected)?.message ?: inputError)
        if (couponState is CouponUiState.Checking) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 16.dp))
            Text("쿠폰 정보를 확인하고 있어요.", color = Muted)
        } else SaveButton("쿠폰 확인") {
            inputError = if (code.isBlank()) "쿠폰 번호를 입력해 주세요." else null
            if (inputError == null) onAction(OwnerAction.CheckCoupon(code))
        }
        return
    }
    val coupon = couponState.coupon
    TextButton(onClick = { onAction(OwnerAction.OpenCoupon) }) { Text("다른 쿠폰 확인") }
    Column(Modifier.fillMaxWidth().padding(top = 16.dp).background(Sage, RoundedCornerShape(15.dp)).padding(17.dp)) {
        Text("확인된 쿠폰", color = Forest, style = MaterialTheme.typography.titleSmall)
        Text(coupon.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
        Text("${coupon.customerLabel} · ${coupon.maskedCode}", style = MaterialTheme.typography.bodySmall, color = Muted)
        Text(coupon.conditionText, modifier = Modifier.padding(top = 8.dp))
        Text("${coupon.scheduleText}\n${coupon.expiresText}", style = MaterialTheme.typography.bodySmall, color = Muted)
    }
    FormField("할인 전 주문 금액 (원)", total, { total = it.filter(Char::isDigit).take(8); inputError = null }, numeric = true)
    val orderAmount = total.moneyValue()
    val quote = couponState.quote?.takeIf { it.orderAmount == orderAmount }
    if (couponState.quoteLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
    else OutlinedButton(onClick = {
        inputError = if (orderAmount == null) "주문 금액을 숫자로 입력해 주세요." else null
        if (orderAmount != null) onAction(OwnerAction.QuoteCoupon(coupon.id, orderAmount))
    }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), enabled = !LocalOwnerSubmitting.current) { Text("할인 금액 확인") }
    if (quote != null) {
        Text("할인 ${quote.discountAmount.won()} · 결제 예정 ${quote.payableAmount.won()}", color = Forest, modifier = Modifier.padding(top = 12.dp))
    }
    FormError(couponState.error ?: inputError)
    Button(onClick = { if (quote != null) onAction(OwnerAction.RedeemCoupon(coupon.id, quote.id)) }, enabled = quote?.canRedeem == true && !LocalOwnerSubmitting.current && !couponState.quoteLoading, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 52.dp), shape = RoundedCornerShape(13.dp)) { Text("쿠폰 사용 완료 처리") }
}

@Composable internal fun StatusPanel(shop: Shop, onSelect: (BusinessStatus) -> Unit) {
    BusinessStatus.entries.forEach { status ->
        Surface(onClick = { onSelect(status) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(14.dp), color = if (shop.status == status) Sage else CanvasColor) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = shop.status == status, onClick = { onSelect(status) })
                Column(Modifier.padding(start = 6.dp)) {
                    Text(status.label, style = MaterialTheme.typography.titleSmall)
                    Text(when(status) { BusinessStatus.OPEN -> "지금 손님을 맞이할 수 있어요"; BusinessStatus.BREAK -> "잠시 쉬거나 재료를 준비하고 있어요"; BusinessStatus.CLOSED -> "임시 휴무 또는 오늘 영업을 마쳤어요" }, color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    Text("일시적으로 설정한 상태입니다. 다시 영업할 때 직접 변경해 주세요.", color = Muted, style = MaterialTheme.typography.bodySmall)
}

@Composable internal fun ReviewPanel(reviews: List<Review>, pending: Int, onReply: (String, String) -> Unit) {
    if (reviews.isEmpty()) {
        Text("아직 등록된 리뷰가 없어요.", color = Muted)
        return
    }
    Badge("답글을 기다리는 리뷰 $pending")
    reviews.forEach { review ->
        var editing by rememberSaveable(review.id) { mutableStateOf(false) }
        var draft by rememberSaveable(review.id, review.reply) { mutableStateOf(review.reply) }
        Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.nickname, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(review.date.replace('-', '.'), style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Text("★".repeat(review.stars), color = Amber, fontSize = 13.sp, modifier = Modifier.semantics { contentDescription = "별점 ${review.stars}점" })
            Text(review.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 7.dp))
            if (review.reply.isNotBlank() && !editing) {
                Column(Modifier.fillMaxWidth().padding(top = 10.dp).background(Sage, RoundedCornerShape(12.dp)).padding(13.dp)) {
                    Text("사장님 답글", style = MaterialTheme.typography.labelMedium, color = Forest)
                    Text(review.reply, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (editing) {
                FormField("${review.nickname}님에게 답글", draft, { draft = it.take(500) }, multiline = true)
                Text("${draft.length}/500", style = MaterialTheme.typography.bodySmall, color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { editing = false; draft = review.reply }) { Text("취소") }
                    Button(onClick = { onReply(review.id, draft.trim()) }, enabled = draft.isNotBlank() && !LocalOwnerSubmitting.current) { Text("답글 저장") }
                }
            } else TextButton(onClick = { editing = true }) { Icon(OwnerIcons.ModeComment, null, Modifier.size(16.dp)); Text(if (review.reply.isBlank()) " 답글 쓰기" else " 답글 수정") }
            HorizontalDivider(Modifier.padding(top = 10.dp), color = Line)
        }
    }
}
@Composable internal fun NoticePanel(state: OwnerState, onPublish: (String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    Text("신메뉴나 오늘의 소식을 손님에게 알려보세요.", color = Muted)
    FormField("소식 제목", title, { title = it.take(50) })
    FormField("소식 내용", body, { body = it.take(500) }, multiline = true)
    FormError(error)
    SaveButton("소식 등록") {
        error = when { title.isBlank() -> "소식 제목을 입력해 주세요."; body.isBlank() -> "소식 내용을 입력해 주세요."; else -> null }
        if (error == null) { onPublish(title, body) }
    }
    if (state.notices.isNotEmpty()) {
        SectionTitle("등록한 소식")
        state.notices.forEach { notice ->
            Text(notice.title, style = MaterialTheme.typography.titleSmall)
            Text(notice.body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 7.dp))
            Text(notice.date, style = MaterialTheme.typography.bodySmall, color = Muted)
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Line)
        }
    }
}
@Composable internal fun StatsPanel(state: OwnerState) {
    val stats = state.stats
    Text("최근 가게 조회", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 22.dp))
    val scale = stats.dailyViews.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    Row(Modifier.fillMaxWidth().height(145.dp).padding(top = 16.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.dailyViews.forEachIndexed { index, day ->
            Column(Modifier.weight(1f).semantics { contentDescription = "${day.dayLabel}: 가게 조회 ${day.count}회" }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day.count.toString(), style = MaterialTheme.typography.bodySmall, color = Muted)
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth().height((day.count.coerceAtLeast(0) / scale.toFloat() * 88).dp).background(if(index == stats.dailyViews.lastIndex) Forest else Sage, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)))
                Text(day.dayLabel, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
    HorizontalDivider(Modifier.padding(vertical = 22.dp), color = Line)
    listOf("오늘 가게 조회" to stats.viewsToday, "오늘 길찾기 클릭" to stats.directionsToday, "쿠폰 발급 누적" to stats.issuedTotal, "쿠폰 사용 누적" to stats.usedTotal, "쿠폰 사용률" to stats.useRate).forEach { (label, value) ->
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) { Text(label, color = Muted, modifier = Modifier.weight(1f)); Text(value, fontWeight = FontWeight.Bold) }
    }
}
