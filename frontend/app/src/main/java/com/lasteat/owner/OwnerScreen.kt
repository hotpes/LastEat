package com.lasteat.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Copy this source into the host Compose app. Inherits the host typography.
 * The host owns API calls, image loading, photo selection/upload and all results.
 */
@Composable
fun OwnerScreen(
    state: OwnerState,
    onAction: (OwnerAction) -> Unit,
    imageContent: OwnerImageContent = { url, description, modifier -> OwnerImagePlaceholder(url, description, modifier) },
    onPickImage: OwnerPickImage? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    OwnerTheme {
        CompositionLocalProvider(
            LocalOwnerImageContent provides imageContent,
            LocalOwnerPickImage provides onPickImage,
        ) {
            OwnerScreenContent(state, onAction, snackbarHostState)
        }
    }
}

/** UI-only entry point: data in, typed intents out. No persistence or API calls. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun OwnerScreenContent(state: OwnerState, onAction: (OwnerAction) -> Unit, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    var panel by rememberSaveable { mutableStateOf(OwnerPanel.NONE) }
    var editingOffer by rememberSaveable { mutableStateOf<String?>(null) }
    val open: (OwnerPanel) -> Unit = { target ->
        editingOffer = null
        panel = target
        if (target == OwnerPanel.REDEEM) onAction(OwnerAction.OpenCoupon)
        val section = when(target) {
            OwnerPanel.SHOP, OwnerPanel.STATUS -> OwnerSection.SHOP
            OwnerPanel.MENUS -> OwnerSection.MENUS
            OwnerPanel.HOURS -> OwnerSection.HOURS
            OwnerPanel.BENEFIT -> OwnerSection.BENEFITS
            OwnerPanel.REVIEWS -> OwnerSection.REVIEWS
            OwnerPanel.STATS -> OwnerSection.STATS
            OwnerPanel.NOTICES -> OwnerSection.NOTICES
            OwnerPanel.ALERTS -> OwnerSection.ALERTS
            else -> null
        }
        section?.let { onAction(OwnerAction.LoadSection(it)) }
    }
    LaunchedEffect(state.result?.id) {
        state.result?.takeIf { it.success }?.let { panel = OwnerPanel.NONE; snackbarHostState.showSnackbar(it.message) }
    }
    Scaffold(
        modifier = Modifier.testTag("ownerScreen"), containerColor = CanvasColor,
        topBar = {
            Surface(color = Color.White) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(61.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(Forest), contentAlignment = Alignment.Center) { Icon(OwnerIcons.Restaurant, null, Modifier.size(19.dp), tint = Color.White) }
                    Text(" LastEat", fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Ink)
                    Spacer(Modifier.width(9.dp)); Badge("사장님"); Spacer(Modifier.weight(1f))
                    BadgedBox(badge = { if(state.reviewSummary.pending > 0) Box(Modifier.size(5.dp).clip(CircleShape).background(Forest)) }) {
                        IconButton(onClick = { open(OwnerPanel.ALERTS) }) { Icon(OwnerIcons.NotificationsNone, "알림", tint = Ink) }
                    }
                }
            }
        }, snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            if (state.isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center).testTag("ownerLoading"))
            else OwnerPage(state, open, onOffer = { editingOffer = it; panel = OwnerPanel.BENEFIT }, onPause = { id ->
                val offer = state.offers.firstOrNull { it.id == id }
                if (offer != null) onAction(OwnerAction.SetBenefitIssuance(id, !offer.paused))
            }, modifier = Modifier.widthIn(max = 560.dp))
        }
    }
    if (panel != OwnerPanel.NONE) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val screenHeight = LocalConfiguration.current.screenHeightDp
        ModalBottomSheet(onDismissRequest = { panel = OwnerPanel.NONE }, sheetState = sheetState, containerColor = Color.White, dragHandle = { BottomSheetDefaults.DragHandle() }) {
            Column(Modifier.testTag("ownerSheet").fillMaxWidth().heightIn(max = (screenHeight * .85f).dp).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 28.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(when(panel) {
                        OwnerPanel.REDEEM -> "쿠폰 확인·사용 처리"; OwnerPanel.SHOP -> "가게 정보 수정"; OwnerPanel.MENUS -> "메뉴 관리"; OwnerPanel.HOURS -> "영업시간·휴무일"; OwnerPanel.BENEFIT -> if (editingOffer == null) "새 혜택 만들기" else "혜택 수정"; OwnerPanel.REVIEWS -> "리뷰와 사장님 답글"; OwnerPanel.STATS -> "우리 가게 성과"; OwnerPanel.NOTICES -> "가게 소식"; OwnerPanel.STATUS -> "지금 영업 상태"; else -> "알림"
                    }, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { panel = OwnerPanel.NONE }) { Icon(OwnerIcons.Close, "닫기") }
                }
                FormError(state.result?.takeUnless { it.success }?.message)
                if (state.isSubmitting) LinearProgressIndicator(Modifier.fillMaxWidth().padding(bottom = 12.dp))
                CompositionLocalProvider(LocalOwnerSubmitting provides state.isSubmitting) {
                    when(panel) {
                        OwnerPanel.REDEEM -> RedeemPanel(state.coupon, onAction) { panel = OwnerPanel.NONE }
                        OwnerPanel.SHOP -> ShopForm(state.shop) { onAction(OwnerAction.SaveShop(ShopDraft(it.name, it.introduction, it.address, it.phone, it.photoUrl))) }
                        OwnerPanel.HOURS -> HoursForm(state.shop) { onAction(OwnerAction.SaveHours(HoursDraft(it.opens, it.closes, it.closedDays))) }
                        OwnerPanel.MENUS -> MenuManager(state, onSave = { onAction(OwnerAction.SaveMenu(it.id.takeIf { id -> id.isNotBlank() }, MenuDraft(it.name, it.price, it.description, it.photoUrl))) }, onToggle = { id, soldOut -> onAction(OwnerAction.SetMenuSoldOut(id, soldOut)) }, onDelete = { onAction(OwnerAction.DeleteMenu(it)) })
                        OwnerPanel.BENEFIT -> BenefitForm(state.offers.find { it.id == editingOffer }) { onAction(OwnerAction.SaveBenefit(it.id.takeIf { id -> id.isNotBlank() }, BenefitDraft(it.terms, it.quantity))) }
                        OwnerPanel.REVIEWS -> ReviewPanel(state.reviews, state.reviewSummary.pending) { id, text -> onAction(OwnerAction.SaveReviewReply(id, text)) }
                        OwnerPanel.NOTICES -> NoticePanel(state) { title, body -> onAction(OwnerAction.PublishNotice(title, body)) }
                        OwnerPanel.STATS -> StatsPanel(state)
                        OwnerPanel.STATUS -> StatusPanel(state.shop) { onAction(OwnerAction.SetBusinessStatus(it)) }
                        OwnerPanel.ALERTS -> {
                            WhiteCard {
                                SmallIcon(OwnerIcons.RateReview)
                                Text(if (state.reviewSummary.pending > 0) "답글을 기다리는 손님이 있어요" else "새로운 알림이 없어요", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                                if (state.reviewSummary.pending > 0) Text("아직 답글이 없는 리뷰 ${state.reviewSummary.pending}건", color = Muted)
                                TextButton(onClick = { open(OwnerPanel.REVIEWS) }) { Text("리뷰 확인하기"); Icon(OwnerIcons.ChevronRight, null) }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}
