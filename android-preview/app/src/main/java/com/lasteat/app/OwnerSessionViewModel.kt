package com.lasteat.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasteat.app.ui.owner.OwnerReadException
import java.io.IOException
import kotlinx.coroutines.launch

internal sealed interface OwnerAccess {
    data class SignIn(val message: String? = null) : OwnerAccess
    data object Checking : OwnerAccess
    data object Denied : OwnerAccess
    data object TryAgain : OwnerAccess
    class Granted(val accessToken: String) : OwnerAccess
}

internal class OwnerSessionViewModel(
    private val api: OwnerAuthApi = OwnerAuthApi(),
) : ViewModel() {
    var access by mutableStateOf<OwnerAccess>(OwnerAccess.SignIn())
        private set

    private var tokens: LastEatTokens? = null
    private var retryOwnerRead = true

    fun enterOwner() {
        if (access == OwnerAccess.Checking) return
        retryOwnerRead = true
        val current = tokens
        if (current == null) {
            access = OwnerAccess.SignIn()
            return
        }
        request(onFailure = OwnerAccess.TryAgain) {
            val role = try {
                api.role(current.accessToken)
            } catch (error: AuthRequestException) {
                if (error.statusCode != 401) throw error
                return@request refreshAndCheck(current)
            }
            checkRole(current, role)
        }
    }

    fun signIn(requestKakaoToken: suspend () -> String?) {
        retryOwnerRead = true
        request(onFailure = OwnerAccess.SignIn("로그인하지 못했어요. 다시 시도해 주세요.")) {
            val kakaoToken = requestKakaoToken() ?: return@request OwnerAccess.SignIn()
            val login = api.signIn(kakaoToken)
            checkRole(login.tokens, login.role)
        }
    }

    fun onOwnerAccessError(error: OwnerReadException): Boolean {
        if (error.statusCode == 403 && error.errorCode == "FORBIDDEN") {
            tokens = null
            access = OwnerAccess.Denied
            return true
        }
        if (error.statusCode != 401) return false

        val current = tokens
        if (current == null || !retryOwnerRead) {
            access = signInAgain()
        } else {
            retryOwnerRead = false
            request(onFailure = OwnerAccess.TryAgain) { refreshAndCheck(current) }
        }
        return true
    }

    private suspend fun refreshAndCheck(current: LastEatTokens): OwnerAccess {
        return try {
            val renewed = api.refresh(current.refreshToken)
            tokens = renewed
            checkRole(renewed, api.role(renewed.accessToken))
        } catch (error: AuthRequestException) {
            if (error.statusCode != 401) throw error
            signInAgain()
        }
    }

    private fun checkRole(session: LastEatTokens, role: String): OwnerAccess {
        if (role == "OWNER") {
            tokens = session
            return OwnerAccess.Granted(session.accessToken)
        }
        tokens = null
        return OwnerAccess.Denied
    }

    private fun signInAgain(): OwnerAccess {
        tokens = null
        return OwnerAccess.SignIn("로그인이 만료됐어요. 다시 로그인해 주세요.")
    }

    private fun request(onFailure: OwnerAccess, action: suspend () -> OwnerAccess) {
        if (access == OwnerAccess.Checking) return
        access = OwnerAccess.Checking
        viewModelScope.launch {
            access = try {
                action()
            } catch (_: IOException) {
                onFailure
            }
        }
    }
}
