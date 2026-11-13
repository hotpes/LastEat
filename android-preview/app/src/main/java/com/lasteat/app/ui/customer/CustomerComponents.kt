package com.lasteat.app.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasteat.app.ui.customer.CustomerColors as C

@Composable
internal fun Label(text: String, size: Int = 12, color: Color = C.Ink,
    bold: Boolean = false, modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE) {
    Text(text, modifier, color = color, fontSize = size.sp, lineHeight = (size * 1.45).sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}

@Composable
internal fun AssetIcon(file: String, size: Dp = 21.dp, description: String? = null) =
    FigmaImage(file, description, Modifier.size(size))

@Composable
internal fun AssetButton(file: String, description: String, size: Dp = 21.dp,
    onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
        .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center) { AssetIcon(file, size, description) }
}

@Composable
internal fun BrandHeader(saved: Boolean = false) {
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).background(C.Green, RoundedCornerShape(11.dp)), Alignment.Center) {
            AssetIcon(if (saved) SavedAssets.Icon3 else NearbyAssets.Utensils, 18.dp)
        }
        Spacer(Modifier.width(8.dp)); Label("LastEat", 21, bold = true)
        Spacer(Modifier.weight(1f))
        AssetIcon(if (saved) SavedAssets.Icon4 else NearbyAssets.MapPin, 15.dp)
        Spacer(Modifier.width(4.dp)); Label("연남동", 13, bold = true)
        Spacer(Modifier.width(4.dp)); AssetIcon(NearbyAssets.ChevronDown, 16.dp)
    }
}

@Composable
internal fun FilterChipUi(label: String, selected: Boolean, onClick: () -> Unit,
    icon: String? = null) {
    Row(Modifier.height(36.dp).clip(RoundedCornerShape(99.dp))
        .background(if (selected) C.Green else Color.White)
        .border(1.dp, if (selected) C.Green else C.Line, RoundedCornerShape(99.dp))
        .clickable(role = Role.Tab, onClick = onClick).padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        if (icon != null) AssetIcon(icon, 14.dp)
        Label(label, 12, if (selected) Color.White else C.Body, selected)
    }
}

@Composable
internal fun BadgeUi(text: String) {
    Label(text, 11, C.Green, true,
        Modifier.background(C.LightGreen, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
}

@Composable
internal fun RestaurantCard(item: PreviewRestaurant, saved: Boolean,
    onSave: () -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier,
    savedScreen: Boolean = false, photo: String = item.photo) {
    Row(modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "${item.name} 상세 보기", onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        FigmaImage(photo, "${item.name} 음식 사진", Modifier.size(92.dp).clip(RoundedCornerShape(12.dp)), crop = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Label(item.name, 18, bold = true, modifier = Modifier.weight(1f), maxLines = 1)
                Box(Modifier.size(28.dp).clickable(role = Role.Checkbox,
                    onClickLabel = if (saved) "${item.name} 저장 해제" else "${item.name} 저장", onClick = onSave), Alignment.Center) {
                    AssetIcon(if (saved) SavedAssets.Icon8 else NearbyAssets.Bookmark, 19.dp,
                        if (saved) "${item.name} 저장 해제" else "${item.name} 저장")
                }
            }
            Label("${item.category} · ${item.description} · ${item.distance}m", 12, C.Muted, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AssetIcon(if (savedScreen) SavedAssets.Icon9 else NearbyAssets.Star, 13.dp)
                Label(item.rating, 12, bold = true); Label("(${item.reviews})", 11, C.Muted)
                Label("영업 중", 11, C.Green)
            }
            if (savedScreen) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Label(if (item.category == "카페") "커피 한 잔" else item.collection, 10, C.Muted)
                    if (item.benefit.isNotBlank() && item.id == "ongi") Label(item.benefit, 10, C.Green, true,
                        Modifier.background(C.LightGreen, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                }
            } else if (item.benefit.isNotBlank()) BadgeUi(item.benefit)
        }
    }
}

@Composable
internal fun CustomerNavigation(saved: Boolean, onExplore: () -> Unit, onSaved: () -> Unit) {
    HorizontalDivider(color = C.Line)
    Row(Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 10.dp)) {
        val names = listOf("주변 탐색", "혜택·이벤트", "저장", "My")
        val icons = if (saved) listOf(SavedAssets.Icon10, SavedAssets.Icon11, SavedAssets.Icon12, SavedAssets.Icon13)
            else listOf(NearbyAssets.Map, NearbyAssets.Ticket, NearbyAssets.Bookmark1, NearbyAssets.UserRound)
        names.forEachIndexed { index, name ->
            val selected = if (saved) index == 2 else index == 0
            Column(Modifier.weight(1f).fillMaxHeight().clickable(enabled = index == 0 || index == 2,
                role = Role.Tab, onClick = { if (index == 0) onExplore() else onSaved() }),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(54.dp, 28.dp).background(if (selected) C.LightGreen else Color.White,
                    RoundedCornerShape(20.dp)), Alignment.Center) { AssetIcon(icons[index], 21.dp) }
                Spacer(Modifier.height(3.dp)); Label(name, 10, if (selected) C.Green else C.Muted, selected)
            }
        }
    }
}

@Composable
internal fun SectionBreak() = Spacer(Modifier.fillMaxWidth().height(8.dp).background(C.Section))

@Composable
internal fun SheetGrip() {
    Box(Modifier.fillMaxWidth().height(22.dp), Alignment.Center) {
        Box(Modifier.size(36.dp, 4.dp).background(Color(0xFFD9DED6), RoundedCornerShape(4.dp)))
    }
}
