package com.lasteat.app.ui.customer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasteat.app.ui.customer.CustomerColors as C

/** Standalone UI entry point. State is sample-only and can be replaced by an API host later. */
@Composable
fun CustomerPreviewApp() {
    var destination by rememberSaveable { mutableStateOf("explore") }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("전체") }
    var results by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var benefitsOnly by rememberSaveable { mutableStateOf(false) }
    var openOnly by rememberSaveable { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf("ongi") }
    var savedIds by rememberSaveable { mutableStateOf(listOf("ongi", "gyeol", "sodam", "green")) }
    val toggle: (String) -> Unit = { id -> savedIds = if (id in savedIds) savedIds - id else savedIds + id }
    val filtered = PreviewRestaurants.filter { item ->
        (category == "전체" || category == item.category) && (!benefitsOnly || item.benefit.isNotBlank()) &&
            (query.isBlank() || query.trim().split(Regex("\\s+")).all { term ->
                "${item.name} ${item.description} ${item.category} 연남동".contains(term,ignoreCase = true)
            })
    }
    BackHandler(detailId != null || results) {
        if (detailId != null) detailId = null else { results = false; expanded = false; query = "" }
    }
    CustomerTheme {
        Column(Modifier.fillMaxSize().background(Color.White).windowInsetsPadding(WindowInsets.systemBars)
            .imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.fillMaxSize().widthIn(max = 600.dp)) {
                if (detailId != null) {
                    val item = PreviewRestaurants.first { it.id == detailId }
                    RestaurantDetail(item, item.id in savedIds, { toggle(item.id) }, { detailId = null })
                } else {
                    Box(Modifier.weight(1f)) {
                        if (destination == "saved") SavedPage(savedIds, toggle, { detailId = it })
                        else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            if (!results) BrandHeader()
                            if (results) {
                                SearchBar(query, { query = it }, { results = true }, true,
                                    { results = false; expanded = false; query = "" })
                                Row(Modifier.padding(horizontal = 20.dp,vertical = 8.dp).horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    FilterChipUi(category,true,{showFilters = true},ResultsAssets.Check)
                                    FilterChipUi("영업 중",openOnly,{openOnly = !openOnly})
                                    FilterChipUi("혜택 있는 곳",benefitsOnly,{benefitsOnly = !benefitsOnly})
                                    FilterChipUi("필터",false,{showFilters = true},ResultsAssets.SlidersHorizontal)
                                }
                                if (!expanded) PreviewMap(true,filtered.map { it.id }.toSet(),selectedId,
                                    { selectedId = it })
                            } else {
                                Box {
                                    PreviewMap(false, filtered.map { it.id }.toSet(), selectedId,
                                        { selectedId = it; category = PreviewRestaurants.first { item -> item.id == it }.category; results = true })
                                    Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                                        SearchBar(query, { query = it }, { results = true })
                                        Spacer(Modifier.height(12.dp))
                                        CuisineFilters(category, { category = it; results = true })
                                    }
                                }
                            }
                            if (results) ResultsPage(filtered, selectedId, savedIds, toggle,
                                { detailId = it }, expanded, { expanded = !expanded })
                            else NearbyRecommendations(filtered.firstOrNull { it.id == selectedId } ?: filtered.firstOrNull(),
                                savedIds, toggle, { detailId = it }, { results = true })
                        }
                    }
                    CustomerNavigation(destination == "saved",
                        { destination = "explore" }, { destination = "saved" })
                }
            }
        }
        if(showFilters) AlertDialog(onDismissRequest = {showFilters = false},
            title = {Label("카테고리",20,bold = true)},
            text = {CuisineFilters(category,{category = it})},
            confirmButton = {TextButton(onClick = {showFilters = false}) {Text("적용")}})
    }
}

@Composable
private fun SearchBar(query: String, onQuery: (String) -> Unit, onSearch: () -> Unit,
    results: Boolean = false, onBack: () -> Unit = {}) {
    val keyboard = LocalSoftwareKeyboardController.current
    Row(if(results) Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 12.dp) else Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        if(results) AssetButton(ResultsAssets.ArrowLeft,"검색 닫기",22.dp,onBack)
        Row(Modifier.weight(1f).height(if(results) 44.dp else 50.dp)
            .then(if(!results) Modifier.shadow(4.dp,RoundedCornerShape(16.dp)) else Modifier)
            .background(if(results) Color(0xFFF2F4EF) else Color.White,RoundedCornerShape(16.dp))
            .padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            if(!results) { AssetIcon(NearbyAssets.Search,22.dp); Spacer(Modifier.width(10.dp)) }
            BasicTextField(query, onQuery, Modifier.weight(1f), singleLine = true,
                textStyle = TextStyle(color = C.Ink,fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); onSearch() }),
                decorationBox = { field ->
                    Box { if(query.isEmpty()) Label("오늘은 어떤 맛을 찾으세요?",14,C.Muted); field() }
                })
            if(query.isNotEmpty()) AssetButton(ResultsAssets.X,"검색어 지우기",16.dp) { onQuery("") }
            else if(!results) AssetButton(NearbyAssets.ArrowRight,"검색",16.dp) { keyboard?.hide(); onSearch() }
        }
    }
}

@Composable
private fun CuisineFilters(category: String, onCategory: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf("전체","한식","양식","일식","카페").forEach {
            FilterChipUi(it,category == it,{ onCategory(it) })
        }
    }
}

@Composable
private fun NearbyRecommendations(item: PreviewRestaurant?, saved: List<String>, toggle: (String) -> Unit,
    open: (String) -> Unit, results: () -> Unit) {
    Column(Modifier.fillMaxWidth().heightIn(min = 376.dp).background(Color.White,
        RoundedCornerShape(topStart = 28.dp,topEnd = 28.dp))) {
        SheetGrip()
        Column(Modifier.padding(start = 20.dp,end = 20.dp,top = 4.dp,bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Label("가까운 곳에서, 맛있는 발견",21,bold = true)
                    Spacer(Modifier.height(4.dp)); Label("내 주변 500m · 지금 영업 중인 24곳",12,C.Muted)
                }
                AssetButton(NearbyAssets.SlidersHorizontal,"주변 식당 필터",22.dp,results)
            }
            if(item != null) RestaurantCard(item,item.id in saved,{toggle(item.id)},{open(item.id)})
            Row(Modifier.fillMaxWidth().height(64.dp).background(C.LightGreen,RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssetIcon(NearbyAssets.TicketPercent,25.dp)
                Column(Modifier.weight(1f)) {
                    Label("맛있는 동네, 반가운 혜택",13,C.DeepGreen,true)
                    Label("연남동 제휴 식당 12곳의 혜택을 만나보세요",11,C.Green)
                }
                AssetIcon(NearbyAssets.ChevronRight,17.dp)
            }
            Row(Modifier.fillMaxWidth().height(30.dp).clickable(onClick = results),
                verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
                Label("주변 식당 모두 보기",13,bold = true); Spacer(Modifier.width(6.dp))
                AssetIcon(NearbyAssets.ArrowRight,16.dp)
            }
        }
    }
}

@Composable
private fun ResultsPage(items: List<PreviewRestaurant>, selected: String, saved: List<String>,
    toggle: (String) -> Unit, open: (String) -> Unit, expanded: Boolean, expand: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(topStart = 24.dp,topEnd = 24.dp))) {
        SheetGrip()
        Column(Modifier.padding(horizontal = 20.dp),verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {
                Label("주변 식당",18,bold = true); Label("  ${items.size}",15,C.Green,true)
                Spacer(Modifier.weight(1f)); Label("가까운 순",12,C.Body); AssetIcon(ResultsAssets.ChevronDown,14.dp)
            }
            if(items.isEmpty()) Column(Modifier.fillMaxWidth().padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                AssetIcon(NearbyAssets.Search,28.dp); Spacer(Modifier.height(12.dp))
                Label("검색 결과가 없어요",18,bold = true); Label("다른 검색어나 카테고리를 선택해 보세요",12,C.Muted)
            }
            val ordered = items.sortedBy { if(it.id == selected) -1 else it.distance }
            ordered.forEachIndexed { index,item ->
                if(index == 0) Column(Modifier.fillMaxWidth().border(1.5.dp,C.Green,RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FBF6),RoundedCornerShape(16.dp)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RestaurantCard(item,item.id in saved,{toggle(item.id)},{open(item.id)})
                    Row(Modifier.fillMaxWidth().height(35.dp).background(C.LightGreen,RoundedCornerShape(8.dp))
                        .clickable { open(item.id) }.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Label("${item.name} 상세정보 보기",12,C.Green,true,Modifier.weight(1f))
                        AssetIcon(ResultsAssets.ArrowRight,16.dp)
                    }
                } else if(expanded || index == 1) {
                    RestaurantCard(item,item.id in saved,{toggle(item.id)},{open(item.id)},
                        photo = if(item.id == "sodam") ResultsAssets.NoodleDishPhoto else item.photo)
                    if(item.id == "sodam") Label("담백한 멸치육수와 매일 뽑는 생면",11,C.Muted)
                }
            }
            Row(Modifier.fillMaxWidth().height(42.dp).clickable(onClick = expand),
                horizontalArrangement = Arrangement.Center,verticalAlignment = Alignment.CenterVertically) {
                AssetIcon(ResultsAssets.List,17.dp); Spacer(Modifier.width(6.dp))
                Label(if(expanded) "지도 다시 보기" else "목록 더보기",12,C.Green,true)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SavedPage(saved: List<String>, toggle: (String) -> Unit, open: (String) -> Unit) {
    var filter by rememberSaveable { mutableStateOf("전체") }
    var collection by rememberSaveable { mutableStateOf<String?>(null) }
    var reverse by rememberSaveable { mutableStateOf(false) }
    var showCollectionDialog by remember { mutableStateOf(false) }
    val allSaved = PreviewRestaurants.filter { it.id in saved }
    val items = allSaved.filter { (filter == "전체" || filter == "영업 중" || it.category == filter) &&
        (collection == null || it.collection == collection) }.let { if(reverse) it.reversed() else it }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BrandHeader(true)
        Column(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 20.dp,vertical = 12.dp)) {
            Label("저장",24,bold = true); Spacer(Modifier.height(3.dp)); Label("다음에 갈 맛집, 한곳에 모아두세요",12,C.Muted)
        }
        Column(Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().height(20.dp),verticalAlignment = Alignment.CenterVertically) {
                Label("내 컬렉션",14,bold = true); Label(" 2",14,C.Green,true); Spacer(Modifier.weight(1f))
                Row(Modifier.clickable { showCollectionDialog = true },verticalAlignment = Alignment.CenterVertically) {
                    AssetIcon(SavedAssets.Plus,14.dp); Label(" 새 컬렉션",12,C.Green,true)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("든든한 한 끼","분위기 좋은 카페").forEachIndexed { index,title ->
                    Row(Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(12.dp))
                        .background(if(collection == title) C.LightGreen else C.Section)
                        .clickable { collection = if(collection == title) null else title }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(32.dp,36.dp).background(C.LightGreen,RoundedCornerShape(8.dp)),Alignment.Center) {
                            AssetIcon(if(index == 0) SavedAssets.Icon6 else SavedAssets.Coffee,19.dp)
                        }
                        Column { Label(title,13,bold = true,maxLines = 1)
                            Label("식당 ${allSaved.count { it.collection == title }}곳",11,C.Muted) }
                    }
                }
            }
        }
        SectionBreak()
        Row(Modifier.fillMaxWidth().height(56.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("전체","한식","카페","영업 중").forEach { label ->
                FilterChipUi(if(label == "전체") "전체 ${allSaved.size}" else label,filter == label,{filter = label})
            }
        }
        Row(Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 20.dp),verticalAlignment = Alignment.CenterVertically) {
            Label("저장한 식당",13,bold = true); Label(" ${items.size}",13,C.Green,true)
            Spacer(Modifier.weight(1f))
            Row(Modifier.clickable { reverse = !reverse },verticalAlignment = Alignment.CenterVertically) {
                Label(if(reverse) "오래된 저장순" else "최근 저장순",11,C.Muted); AssetIcon(SavedAssets.Icon7,14.dp)
            }
        }
        Column(Modifier.padding(horizontal = 20.dp)) {
            if(items.isEmpty()) Column(Modifier.fillMaxWidth().padding(vertical = 60.dp),horizontalAlignment = Alignment.CenterHorizontally) {
                AssetIcon(NearbyAssets.Bookmark1,32.dp); Spacer(Modifier.height(12.dp))
                Label("아직 저장한 식당이 없어요",18,bold = true); Label("마음에 드는 식당의 저장 아이콘을 눌러보세요",12,C.Muted)
            }
            items.forEach { item ->
                RestaurantCard(item,true,{toggle(item.id)},{open(item.id)},Modifier.height(124.dp),true)
                HorizontalDivider(color = C.Line)
            }
        }
    }
    if(showCollectionDialog) AlertDialog(onDismissRequest = {showCollectionDialog = false},
        title = {Label("내 컬렉션",20,bold = true)},
        text = {Label("식당을 저장하고 컬렉션별로 모아보세요.",14,C.Body)},
        confirmButton = {TextButton(onClick = {showCollectionDialog = false}) {Text("확인")}})
}
