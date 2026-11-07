package com.lasteat.app.ui.onboarding

import com.lasteat.owner.BenefitKind
import com.lasteat.owner.BenefitTerms
import org.json.JSONArray
import org.json.JSONObject

enum class AppRole { Customer, Owner }

/** 동네 선택 목록 항목 */
data class Neighborhood(val name: String, val region: String)

/**
 * 사장님 식당 정보.
 * 검색으로 찾은 기존 식당이면 id 가 있고, 직접 등록 신청이면 id 가 null 입니다.
 * opens/closes/closedDays 는 사장님 화면(Owner UI)의 영업시간 형식과 같습니다. (요일 1=월 … 7=일)
 */
data class ShopInfo(
    val id: String?,
    val name: String,
    val address: String,
    val detailAddress: String,
    val category: String,
    val phone: String,
    val hoursText: String,
    val opens: String = "",
    val closes: String = "",
    val closedDays: Set<Int> = emptySet(),
)

/** 사업자 인증 입력값. 파일은 기기 안 주소(uri)만 들고 있고, 업로드는 백엔드 연동 때 합니다. */
data class BizInfo(
    val number: String,
    val ownerName: String,
    val companyName: String,
    val mobile: String,
    val fileName: String,
    val fileUri: String,
)

/** 사장님 신청 한 건 (식당 + 사업자 인증 + 첫 혜택) */
data class OwnerApplication(
    val shop: ShopInfo,
    val biz: BizInfo,
    val benefit: BenefitTerms?,
    val benefitQuantity: Int,
    val submittedAt: String, // 2026.11.04
)

// ─────────────────────────────────────────────────────────────
// 시연용 검색 데이터 — 백엔드 연동 시 각각 API 로 교체
//  · 동네 검색: 주소 검색 API (카카오 로컬 등)
//  · 식당 검색: GET /stores?keyword=
// ─────────────────────────────────────────────────────────────
object OnboardingSamples {
    val neighborhoods = listOf(
        Neighborhood("연남동", "서울특별시 마포구"),
        Neighborhood("연희동", "서울특별시 서대문구"),
        Neighborhood("연남로", "서울특별시 마포구"),
        Neighborhood("망원동", "서울특별시 마포구"),
        Neighborhood("서교동", "서울특별시 마포구"),
        Neighborhood("합정동", "서울특별시 마포구"),
        Neighborhood("동교동", "서울특별시 마포구"),
        Neighborhood("성수동", "서울특별시 성동구"),
        Neighborhood("을지로동", "서울특별시 중구"),
        Neighborhood("익선동", "서울특별시 종로구"),
    )

    val shops = listOf(
        ShopInfo("s1", "연남 다정식탁", "서울 마포구 동교로46길 12", "1층 (연남동)", "한식 · 일반음식점", "02-332-1024", "화–일 11:30–21:00\n월요일 정기 휴무", "11:30", "21:00", setOf(1)),
        ShopInfo("s2", "다정한 연남", "서울 마포구 성미산로 161", "2층", "카페 · 휴게음식점", "02-336-2210", "매일 10:00–22:00", "10:00", "22:00"),
        ShopInfo("s3", "연남 온기", "서울 마포구 동교로38길 12", "1층", "한식 · 일반음식점", "02-332-0428", "11:00–21:00 · 매주 월요일 휴무", "11:00", "21:00", setOf(1)),
        ShopInfo("s4", "소담국수", "서울 마포구 연희로 21", "1층", "한식 · 일반음식점", "02-324-7711", "매일 11:00–20:30", "11:00", "20:30"),
        ShopInfo("s5", "카페 결", "서울 마포구 성미산로 190", "1층", "카페 · 휴게음식점", "02-325-0186", "매일 09:00–21:00", "09:00", "21:00"),
        ShopInfo("s6", "초록식탁", "서울 마포구 동교로 251", "2층", "양식 · 일반음식점", "02-338-1520", "화–일 11:30–21:30", "11:30", "21:30", setOf(1)),
    )

    val categories = listOf(
        "한식 · 일반음식점", "중식 · 일반음식점", "일식 · 일반음식점", "양식 · 일반음식점",
        "분식 · 일반음식점", "카페 · 휴게음식점", "베이커리 · 제과점", "주점 · 일반음식점", "기타",
    )

    /** 띄어쓰기로 나눈 단어가 모두 들어 있으면 결과로 봅니다. ("연남 다정" → 연남 다정식탁, 다정한 연남) */
    fun searchShops(query: String): List<ShopInfo> = search(query, shops) { it.name + " " + it.address }

    fun searchNeighborhoods(query: String): List<Neighborhood> = search(query, neighborhoods) { it.name + " " + it.region }

    private fun <T> search(query: String, items: List<T>, text: (T) -> String): List<T> {
        val words = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        return items.filter { item -> val t = text(item); words.all { t.contains(it) } }
    }
}

// ─────────────────────────────────────────────────────────────
// 기기 저장용 JSON 변환 (Android 기본 org.json 사용, 추가 라이브러리 없음)
// ─────────────────────────────────────────────────────────────
internal fun OwnerApplication.toJson(): String = JSONObject().apply {
    put("shop", JSONObject().apply {
        put("id", shop.id ?: JSONObject.NULL); put("name", shop.name); put("address", shop.address)
        put("detailAddress", shop.detailAddress); put("category", shop.category); put("phone", shop.phone)
        put("hoursText", shop.hoursText); put("opens", shop.opens); put("closes", shop.closes)
        put("closedDays", JSONArray(shop.closedDays.sorted()))
    })
    put("biz", JSONObject().apply {
        put("number", biz.number); put("ownerName", biz.ownerName); put("companyName", biz.companyName)
        put("mobile", biz.mobile); put("fileName", biz.fileName); put("fileUri", biz.fileUri)
    })
    benefit?.let { b ->
        put("benefit", JSONObject().apply {
            put("kind", b.kind.name); put("percent", b.percent); put("amount", b.amount); put("freeItem", b.freeItem)
            put("minimum", b.minimum); put("maximum", b.maximum); put("expires", b.expires)
            put("days", JSONArray(b.days.sorted())); put("starts", b.starts); put("ends", b.ends)
        })
    }
    put("benefitQuantity", benefitQuantity)
    put("submittedAt", submittedAt)
}.toString()

internal fun ownerApplicationFromJson(json: String): OwnerApplication? = runCatching {
    val o = JSONObject(json)
    val s = o.getJSONObject("shop")
    val b = o.getJSONObject("biz")
    fun JSONArray.ints() = (0 until length()).map { getInt(it) }.toSet()
    OwnerApplication(
        shop = ShopInfo(
            id = if (s.isNull("id")) null else s.getString("id"),
            name = s.getString("name"), address = s.getString("address"), detailAddress = s.getString("detailAddress"),
            category = s.getString("category"), phone = s.getString("phone"), hoursText = s.getString("hoursText"),
            opens = s.optString("opens"), closes = s.optString("closes"), closedDays = s.getJSONArray("closedDays").ints(),
        ),
        biz = BizInfo(
            b.getString("number"), b.getString("ownerName"), b.getString("companyName"),
            b.getString("mobile"), b.getString("fileName"), b.getString("fileUri"),
        ),
        benefit = o.optJSONObject("benefit")?.let { t ->
            BenefitTerms(
                percent = t.getInt("percent"), minimum = t.getInt("minimum"), maximum = t.getInt("maximum"),
                expires = t.getString("expires"), kind = BenefitKind.valueOf(t.getString("kind")),
                amount = t.getInt("amount"), freeItem = t.getString("freeItem"),
                days = t.getJSONArray("days").ints(), starts = t.getString("starts"), ends = t.getString("ends"),
            )
        },
        benefitQuantity = o.optInt("benefitQuantity", 0),
        submittedAt = o.getString("submittedAt"),
    )
}.getOrNull()
