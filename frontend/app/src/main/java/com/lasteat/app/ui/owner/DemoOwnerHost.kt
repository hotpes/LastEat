package com.lasteat.app.ui.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lasteat.owner.CouponUiState
import com.lasteat.owner.MenuItem
import com.lasteat.owner.Notice
import com.lasteat.owner.Offer
import com.lasteat.owner.OwnerAction
import com.lasteat.owner.OwnerResult
import com.lasteat.owner.OwnerState
import com.lasteat.owner.Shop
import com.lasteat.app.ui.onboarding.OwnerApplication
import java.time.LocalDate
import java.util.UUID

/**
 * 시연용 사장님 화면 상태 관리자.
 *
 * 팀원 Owner UI 는 서버 호출 없이 OwnerAction 만 내보내고,
 * 저장 결과(OwnerState·OwnerResult)는 호스트가 돌려줘야 화면이 갱신됩니다.
 * 백엔드가 붙기 전까지 이 클래스가 메모리에서 그 역할을 대신합니다.
 *
 * 백엔드 연동 시: 같은 handle(action) 자리에서 API 명세의 /owner/stores/... 를 호출하고
 * 응답으로 state 를 바꾸면 됩니다. 화면 코드는 그대로 둡니다.
 */
class DemoOwnerHost(initialShop: Shop = Shop()) {

    var state by mutableStateOf(OwnerState(shop = initialShop))
        private set

    fun handle(action: OwnerAction) {
        when (action) {
            is OwnerAction.LoadSection -> Unit // 서버 조회 자리
            OwnerAction.OpenCoupon -> state = state.copy(coupon = CouponUiState.Idle)

            is OwnerAction.SaveShop -> {
                val d = action.draft
                state = state.copy(
                    shop = state.shop.copy(name = d.name, introduction = d.introduction, address = d.address, phone = d.phone, photoUrl = d.photoUrl),
                )
                ok("가게 정보를 저장했어요. 이어서 영업시간과 첫 혜택을 등록해 보세요.")
            }

            is OwnerAction.SaveHours -> {
                val d = action.draft
                state = state.copy(shop = state.shop.copy(opens = d.opens, closes = d.closes, closedDays = d.closedDays))
                ok("영업시간을 저장했어요.")
            }

            is OwnerAction.SetBusinessStatus -> {
                state = state.copy(shop = state.shop.copy(status = action.status))
                ok("지금 상태를 '${action.status.label}'(으)로 바꿨어요.")
            }

            is OwnerAction.SaveMenu -> {
                val d = action.draft
                val item = MenuItem(action.id ?: newId(), d.name, d.price, d.description, photoUrl = d.photoUrl)
                val menus = if (action.id == null) state.menus + item else state.menus.map { if (it.id == action.id) item.copy(soldOut = it.soldOut) else it }
                state = state.copy(menus = menus)
                ok("메뉴를 저장했어요.")
            }

            is OwnerAction.SetMenuSoldOut -> {
                state = state.copy(menus = state.menus.map { if (it.id == action.id) it.copy(soldOut = action.soldOut) else it })
                ok(if (action.soldOut) "품절로 표시했어요." else "다시 판매 중으로 바꿨어요.")
            }

            is OwnerAction.DeleteMenu -> {
                state = state.copy(menus = state.menus.filterNot { it.id == action.id })
                ok("메뉴를 삭제했어요.")
            }

            is OwnerAction.SaveBenefit -> {
                val d = action.draft
                // 실제로는 운영진 검토(PENDING) 후 노출됩니다. (API 명세: 혜택 검토 상태)
                val offer = Offer(
                    id = action.id ?: newId(),
                    title = d.terms.headline(),
                    terms = d.terms,
                    quantity = d.quantity,
                    statusText = "검토 중",
                )
                val offers = if (action.id == null) state.offers + offer else state.offers.map { if (it.id == action.id) offer else it }
                state = state.copy(offers = offers)
                ok("혜택 등록을 신청했어요. 검토는 영업일 기준 1~2일 걸려요.")
            }

            is OwnerAction.SetBenefitIssuance -> {
                state = state.copy(offers = state.offers.map { if (it.id == action.id) it.copy(paused = action.paused, statusText = if (action.paused) "발급 중지" else "발급 중") else it })
                ok(if (action.paused) "혜택 노출을 멈췄어요." else "혜택 노출을 다시 시작했어요.")
            }

            is OwnerAction.SaveReviewReply -> {
                state = state.copy(reviews = state.reviews.map { if (it.id == action.reviewId) it.copy(reply = action.text) else it })
                ok("답글을 저장했어요.")
            }

            is OwnerAction.PublishNotice -> {
                state = state.copy(notices = listOf(Notice(action.title, action.body, LocalDate.now().toString())) + state.notices)
                ok("소식을 등록했어요.")
            }

            // 쿠폰은 API 명세 1차 범위에서 제외되어 시연에서는 안내만 합니다.
            is OwnerAction.CheckCoupon, is OwnerAction.QuoteCoupon, is OwnerAction.RedeemCoupon ->
                state = state.copy(coupon = CouponUiState.Rejected("쿠폰 기능은 다음 단계에서 열려요."))
        }
    }

    private fun ok(message: String) {
        state = state.copy(result = OwnerResult(newId(), true, message))
    }

    private fun newId() = UUID.randomUUID().toString()

    companion object {
        /**
         * 온보딩 신청 내용으로 사장님 홈을 채웁니다.
         * 가게 정보·영업시간은 Shop 으로, 첫 혜택은 '검토 중' 혜택으로 들어갑니다.
         */
        fun fromApplication(app: OwnerApplication): DemoOwnerHost {
            val s = app.shop
            val host = DemoOwnerHost(
                Shop(
                    name = s.name,
                    address = listOf(s.address, s.detailAddress).filter { it.isNotBlank() }.joinToString(" "),
                    phone = s.phone,
                    opens = s.opens,
                    closes = s.closes,
                    closedDays = s.closedDays,
                    categoryLabel = s.category,
                    // 직접 등록 신청처럼 시작·종료 시간이 따로 없으면 입력한 문구를 그대로 보여줌
                    hoursSummary = if (s.opens.isBlank()) s.hoursText.replace("\n", " · ") else "",
                ),
            )
            app.benefit?.let { terms ->
                host.state = host.state.copy(
                    offers = listOf(Offer(id = "onboarding-benefit", title = terms.headline(), terms = terms, quantity = app.benefitQuantity, statusText = "검토 중", canToggleIssuance = false)),
                )
            }
            return host
        }
    }
}
