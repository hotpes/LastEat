package com.lasteat.app.ui.owner

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class OwnerReadException(
    val statusCode: Int?,
    val errorCode: String?,
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)

/**
 * Read-only adapter for the owner GET endpoints in LastEat API v0.2.
 * Pass the host app's server URL and authenticated owner access token.
 * The host app must declare android.permission.INTERNET.
 */
class OwnerReadApi(baseUrl: String) {
    private val apiRoot: URI = URI(baseUrl.trim().trimEnd('/')).let { base ->
        require(base.scheme in setOf("http", "https") && base.host != null)
        require(base.rawUserInfo == null && base.rawQuery == null && base.rawFragment == null)
        if (base.path.endsWith("/api/v1")) base else URI(base.toString() + "/api/v1")
    }

    /** Uses the first store from the server when the caller has no selected store. */
    suspend fun load(accessToken: String, storeId: Long? = null): OwnerState =
        withContext(Dispatchers.IO) {
            if (accessToken.isBlank() || accessToken.any { it.isWhitespace() }) {
                throw OwnerReadException(null, null, "사장님 로그인 토큰을 확인해 주세요.")
            }

            try {
                val stores = getArray("/owner/stores", accessToken)
                val selectedId = if (storeId == null) {
                    if (stores.length() == 0) return@withContext OwnerState()
                    stores.getJSONObject(0).getLong("id")
                } else {
                    val ownsStore = (0 until stores.length()).any {
                        stores.getJSONObject(it).getLong("id") == storeId
                    }
                    if (!ownsStore) {
                        throw OwnerReadException(null, null, "선택한 가게를 내 가게 목록에서 찾을 수 없습니다.")
                    }
                    storeId
                }
                val path = "/owner/stores/" + selectedId
                val shop = getObject(path, accessToken).toShop()
                val menus = getArray(path + "/menus", accessToken).objects { item ->
                    MenuItem(
                        id = item.getLong("id").toString(),
                        name = item.getString("name"),
                        price = item.getInt("price"),
                        description = item.stringOrEmpty("description"),
                        soldOut = item.getBoolean("soldOut"),
                        photoUrl = item.stringOrEmpty("imageUrl"),
                    )
                }
                val notices = getArray(path + "/benefits", accessToken).objects { benefit ->
                    Notice(
                        id = benefit.getLong("id").toString(),
                        headline = benefit.getString("title"),
                        condition = benefit.stringOrEmpty("summary"),
                        periodText = benefit.getString("startDate") + " ~ " + benefit.getString("endDate"),
                        isPublished = benefit.getBoolean("visible"),
                    )
                }
                OwnerState(shop = shop, menus = menus, notices = notices)
            } catch (error: JSONException) {
                throw OwnerReadException(null, null, "서버 응답 형식을 확인해 주세요.", error)
            }
        }

    private fun getObject(path: String, accessToken: String): JSONObject =
        get(path, accessToken) as? JSONObject ?: throw JSONException("Expected an object")

    private fun getArray(path: String, accessToken: String): JSONArray =
        get(path, accessToken) as? JSONArray ?: throw JSONException("Expected an array")

    private fun get(path: String, accessToken: String): Any {
        val connection = URI(apiRoot.toString() + path).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer " + accessToken)
            val status = connection.responseCode
            if (status !in 200..299) {
                val body = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                val error = body?.let { runCatching { JSONObject(it).optJSONObject("error") }.getOrNull() }
                throw responseError(status, error)
            }

            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val envelope = JSONObject(body)
            if (!envelope.getBoolean("success")) throw responseError(status, envelope.optJSONObject("error"))
            return envelope.get("data")
        } finally {
            connection.disconnect()
        }
    }

    private fun responseError(status: Int, error: JSONObject?): OwnerReadException =
        OwnerReadException(
            statusCode = status,
            errorCode = error?.stringOrEmpty("code")?.ifBlank { null },
            message = error?.stringOrEmpty("message")?.takeIf { it.isNotBlank() }
                ?: "가게 정보를 불러오지 못했습니다 (HTTP " + status + ").",
        )

    private fun JSONObject.toShop(): Shop {
        val hours = optJSONObject("businessHours")
        val days = hours?.optJSONArray("closedDays")
        val closedDays = buildSet {
            if (days != null) {
                for (index in 0 until days.length()) {
                    val day = dayCodes.indexOf(days.getString(index))
                    if (day >= 0) add(day + 1)
                }
            }
        }
        val status = when (stringOrEmpty("businessStatus")) {
            "OPEN" -> BusinessStatus.OPEN
            "BREAK" -> BusinessStatus.BREAK
            "CLOSED" -> BusinessStatus.CLOSED
            else -> null
        }
        val category = stringOrEmpty("category")
        return Shop(
            name = getString("name"),
            introduction = stringOrEmpty("description"),
            address = getString("address"),
            phone = stringOrEmpty("phone"),
            opens = hours?.stringOrEmpty("openTime").orEmpty(),
            closes = hours?.stringOrEmpty("closeTime").orEmpty(),
            closedDays = closedDays,
            status = status,
            photoUrl = optJSONArray("imageUrls")?.optString(0).orEmpty(),
            categoryLabel = categoryLabels[category] ?: category,
        )
    }

    private fun JSONObject.stringOrEmpty(key: String): String =
        if (isNull(key)) "" else optString(key)

    private inline fun <T> JSONArray.objects(transform: (JSONObject) -> T): List<T> =
        List(length()) { index -> transform(getJSONObject(index)) }

    private companion object {
        val dayCodes = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val categoryLabels = mapOf(
            "KOREAN" to "한식", "WESTERN" to "양식", "JAPANESE" to "일식",
            "CHINESE" to "중식", "CAFE" to "카페", "BAKERY" to "베이커리", "ETC" to "기타",
        )
    }
}

/**
 * Entry point for the existing OwnerScreen with server data.
 * Change refreshKey to reload. The existing button events are passed to onAction.
 * Keep the api instance stable across recompositions.
 */
@Composable
fun OwnerScreenFromServer(
    api: OwnerReadApi,
    accessToken: String,
    storeId: Long? = null,
    refreshKey: Any? = null,
    onAction: (OwnerAction) -> Unit = {},
    imageContent: OwnerImageContent = { url, description, modifier ->
        OwnerImagePlaceholder(url, description, modifier)
    },
    onPickImage: OwnerPickImage? = null,
    onClose: (() -> Unit)? = null,
) {
    var state by remember(api, accessToken, storeId) {
        mutableStateOf(OwnerState(isLoading = accessToken.isNotBlank()))
    }
    val snackbar = remember(api, accessToken, storeId) { SnackbarHostState() }

    LaunchedEffect(api, accessToken, storeId, refreshKey) {
        if (accessToken.isBlank()) {
            state = OwnerState()
            return@LaunchedEffect
        }

        state = state.copy(isLoading = true)
        try {
            state = api.load(accessToken, storeId)
        } catch (error: OwnerReadException) {
            state = state.copy(isLoading = false)
            snackbar.showSnackbar(error.message.orEmpty())
        } catch (error: IOException) {
            state = state.copy(isLoading = false)
            snackbar.showSnackbar("서버에 연결하지 못했습니다.")
        }
    }

    key(api, accessToken, storeId) {
        OwnerScreen(
            state = state,
            onAction = onAction,
            imageContent = imageContent,
            onPickImage = onPickImage,
            snackbarHostState = snackbar,
            onClose = onClose,
        )
    }
}
