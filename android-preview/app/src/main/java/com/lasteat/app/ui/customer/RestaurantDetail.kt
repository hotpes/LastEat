package com.lasteat.app.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lasteat.app.ui.customer.CustomerColors as C

@Composable
internal fun ColumnScope.RestaurantDetail(item: PreviewRestaurant, saved: Boolean,
    toggle: () -> Unit, back: () -> Unit) {
    var tab by rememberSaveable(item.id) { mutableStateOf(0) }
    var action by remember { mutableStateOf<String?>(null) }
    Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp),verticalAlignment = Alignment.CenterVertically) {
        AssetButton(DetailAssets.ArrowLeft,"상세 닫기",22.dp,back)
        Spacer(Modifier.weight(1f)); Label(item.name,15,bold = true); Spacer(Modifier.weight(1f))
        AssetButton(DetailAssets.Share2,"공유",21.dp) {action = "공유"}
        AssetButton(if(saved) SavedAssets.Icon8 else DetailAssets.Bookmark,
            if(saved) "${item.name} 저장 해제" else "${item.name} 저장",21.dp,toggle)
    }
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().height(228.dp),horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FigmaImage(if(item.id == "ongi") DetailAssets.SignatureDish else item.photo,
                "${item.name} 대표 음식",Modifier.weight(270f).fillMaxHeight(),crop = true)
            Column(Modifier.weight(138f),verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FigmaImage(DetailAssets.DiningRoomPhoto,"식당 내부",Modifier.fillMaxWidth().height(112.dp),crop = true)
                Box {
                    FigmaImage(DetailAssets.SecondaryDishPhoto,"추가 음식 사진",Modifier.fillMaxWidth().height(112.dp),crop = true)
                    Label("사진 24",10,Color.White,modifier = Modifier.align(Alignment.BottomEnd).padding(9.dp)
                        .background(C.Ink.copy(alpha = .7f),RoundedCornerShape(6.dp)).padding(horizontal = 7.dp,vertical = 3.dp))
                }
            }
        }
        Column(Modifier.padding(20.dp),verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Label("연남동 · ${item.category} · ${item.description}",12,C.Muted,modifier = Modifier.weight(1f))
                Label("영업 중",12,C.Green,true)
            }
            Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Label(item.name,28,bold = true); BadgeUi("동네 추천")
            }
            Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                AssetIcon(DetailAssets.Star,15.dp); Label(item.rating,13,bold = true)
                Label("리뷰 ${item.reviews} · 내 위치에서 ${item.distance}m",12,C.Body)
            }
            Label(if(item.id == "ongi") "제철 재료로 채운 든든한 한 그릇. 바쁜 하루에 따뜻한 온기를 더해주는 연남동 작은 밥집이에요."
                else "동네에서 만나는 ${item.description}. 오늘도 정성껏 준비한 한 끼를 만나보세요.",13,C.Body)
        }
        Row(Modifier.fillMaxWidth().height(48.dp)) {
            listOf("정보","메뉴","혜택 1").forEachIndexed { index,label ->
                Column(Modifier.weight(1f).fillMaxHeight().clickable {tab = index},horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.weight(1f),Alignment.Center) {Label(label,13,if(tab == index) C.Green else C.Muted,tab == index)}
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if(tab == index) C.Green else C.Line))
                }
            }
        }
        if(tab == 0) {
            Column(Modifier.padding(20.dp),verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoRow(DetailAssets.MapPin,"서울 마포구 동교로38길 12, 1층","홍대입구역 3번 출구에서 도보 6분 · 길찾기 ›")
                InfoRow(DetailAssets.Clock3,"11:00–21:00 · 라스트오더 20:30","브레이크타임 15:30–17:00 · 매주 월요일 휴무")
                InfoRow(DetailAssets.Phone,"02-332-0428")
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {BadgeUi("혼밥 좋아요");BadgeUi("포장 가능");BadgeUi("주차 불가")}
            }
            SectionBreak()
        }
        if(tab != 2) {
            Column(Modifier.padding(20.dp),verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {
                    Label("대표 메뉴",18,bold = true,modifier = Modifier.weight(1f))
                    Label("전체 메뉴 8 ›",12,C.Muted,modifier = Modifier.clickable {tab = 1})
                }
                Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FigmaImage(DetailAssets.DishPhoto,"연어 아보카도 덮밥",Modifier.size(74.dp,70.dp)
                        .clip(RoundedCornerShape(12.dp)),crop = true)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Label("연어 아보카도 덮밥",14,bold = true)
                        Label("구운 연어와 고소한 아보카도의 조화",11,C.Muted)
                        Label("15,000원",14,bold = true)
                    }
                }
                Row {Label("소고기 버섯 솥밥",13,modifier = Modifier.weight(1f));Label("16,000원",13,bold = true)}
            }
            SectionBreak()
        }
        if(tab != 1) Column(Modifier.padding(20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row {Label("챙겨가는 혜택",18,bold = true);Label(" 1",15,C.Green,true)}
            Column(Modifier.fillMaxWidth().background(C.LightGreen,RoundedCornerShape(16.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Label("동네한입 단독 · 10월 제휴 혜택",10,C.Green,true)
                        Label("마감 할인 10%",23,C.DeepGreen,true)
                    }
                    AssetIcon(DetailAssets.TicketPercent,30.dp)
                }
                Label("마감 시간대 · 2만원 이상 결제 · 최대 5천원\n1인 1회 · 다른 할인과 중복 적용 불가",11,C.Body)
                Label("2026.10.01–10.31까지 · 마감 할인 자세히 보기 ›",11,C.Green)
            }
        }
    }
    HorizontalDivider(color = C.Line)
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp,vertical = 12.dp),horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.width(108.dp).height(48.dp).background(C.LightGreen,RoundedCornerShape(12.dp))
            .clickable {action = "전화"},verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
            AssetIcon(DetailAssets.Phone1,18.dp); Spacer(Modifier.width(7.dp)); Label("전화",14,C.Green,true)
        }
        Row(Modifier.weight(1f).height(48.dp).background(C.Green,RoundedCornerShape(12.dp))
            .clickable {action = "길찾기"},verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
            AssetIcon(DetailAssets.Navigation,18.dp); Spacer(Modifier.width(7.dp)); Label("길찾기",14,Color.White,true)
        }
    }
    if(action != null) AlertDialog(onDismissRequest = {action = null},
        title = {Label(action!!,20,bold = true)},
        text = {Label(when(action) {"전화" -> "02-332-0428";"길찾기" -> "서울 마포구 동교로38길 12, 1층";else -> "${item.name} · 연남동 ${item.category}"},14,C.Body)},
        confirmButton = {TextButton(onClick = {action = null}) {Text("닫기")}})
}

@Composable
private fun InfoRow(icon: String, title: String, subtitle: String? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AssetIcon(icon,18.dp)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Label(title,13);if(subtitle != null) Label(subtitle,11,C.Muted)
        }
    }
}
