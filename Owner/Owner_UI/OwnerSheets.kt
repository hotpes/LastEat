package com.lasteat.app.ui.owner

import androidx.compose.foundation.background
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
@Composable internal fun NoticeListPanel(state: OwnerState, onNew: () -> Unit, onEdit: (String) -> Unit, onVisibility: (String, Boolean) -> Unit, onDelete: (String) -> Unit) {
    Text("게시한 할인 안내를 수정하거나 공개 상태를 바꿀 수 있어요.", color = Muted)
    SaveButton("+ 새 할인 공지") { onNew() }
    if (state.notices.isEmpty()) {
        Text("등록된 할인 공지가 없어요.", color = Muted, modifier = Modifier.padding(top = 20.dp))
    }
    state.notices.forEach { notice ->
        Spacer(Modifier.height(16.dp))
        NoticeCard(notice, { onEdit(notice.id) }, { onVisibility(notice.id, !notice.isPublished) }, { onDelete(notice.id) })
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
    listOf("오늘 가게 조회" to stats.viewsToday, "오늘 길찾기 클릭" to stats.directionsToday, "오늘 할인 공지 조회" to stats.noticeViewsToday, "게시된 공지 수" to stats.noticeCount).forEach { (label, value) ->
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) { Text(label, color = Muted, modifier = Modifier.weight(1f)); Text(value, fontWeight = FontWeight.Bold) }
    }
}
