package com.lasteat.app

import com.lasteat.app.ui.owner.LAST_EAT_API_ROOT
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

internal class LastEatTokens(val accessToken: String, val refreshToken: String)

internal class LastEatLogin(val tokens: LastEatTokens, val role: String)

internal class AuthRequestException(val statusCode: Int) : IOException()

internal class OwnerAuthApi(private val apiRoot: String = LAST_EAT_API_ROOT) {
    suspend fun signIn(kakaoToken: String): LastEatLogin =
        request("/auth/kakao", body = JSONObject().put("kakaoAccessToken", kakaoToken)) { data ->
            LastEatLogin(data.toTokens(), data.getJSONObject("member").getString("role"))
        }

    suspend fun refresh(refreshToken: String): LastEatTokens =
        request("/auth/refresh", body = JSONObject().put("refreshToken", refreshToken)) { data ->
            data.toTokens()
        }

    suspend fun role(accessToken: String): String =
        request("/members/me", accessToken = accessToken) { it.getString("role") }

    private suspend fun <T> request(
        path: String,
        body: JSONObject? = null,
        accessToken: String? = null,
        parse: (JSONObject) -> T,
    ): T = withContext(Dispatchers.IO) {
        val connection = URI(apiRoot + path).toURL().openConnection() as HttpURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Accept", "application/json")
            if (accessToken != null) {
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
            }
            if (body != null) {
                val bytes = body.toString().toByteArray(Charsets.UTF_8)
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }

            val status = connection.responseCode
            if (status !in 200..299) throw AuthRequestException(status)
            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val envelope = JSONObject(response)
            if (!envelope.getBoolean("success")) throw AuthRequestException(status)
            parse(envelope.getJSONObject("data"))
        } catch (error: JSONException) {
            throw IOException("Invalid LastEat auth response", error)
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toTokens(): LastEatTokens {
        val access = getString("accessToken")
        val refresh = getString("refreshToken")
        if (access.isBlank() || refresh.isBlank()) throw JSONException("Missing LastEat token")
        return LastEatTokens(access, refresh)
    }
}
