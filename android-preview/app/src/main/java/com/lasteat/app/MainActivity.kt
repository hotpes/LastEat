package com.lasteat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.lasteat.app.ui.customer.CustomerPreviewApp
import com.lasteat.app.ui.customer.CustomerTheme
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class MainActivity : ComponentActivity() {
    private val ownerSession by viewModels<OwnerSessionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val kakaoKey = BuildConfig.KAKAO_NATIVE_APP_KEY
        if (kakaoKey.isNotEmpty()) KakaoSdk.init(applicationContext, kakaoKey)
        setContent {
            RolePreviewApp(
                ownerSession = ownerSession,
                kakaoConfigured = kakaoKey.isNotEmpty(),
                onSignIn = { ownerSession.signIn(::requestKakaoToken) },
            )
        }
    }

    private suspend fun requestKakaoToken(): String? = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.loginWithKakao(this) { token, error ->
            if (continuation.isActive) {
                when {
                    error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                        continuation.resume(null)
                    error != null -> continuation.resumeWithException(IOException("Kakao login failed", error))
                    else -> continuation.resume(token?.accessToken)
                }
            }
        }
    }
}

private enum class PreviewRole { Customer, Owner }

@Composable
private fun RolePreviewApp(
    ownerSession: OwnerSessionViewModel,
    kakaoConfigured: Boolean,
    onSignIn: () -> Unit,
) {
    var role by rememberSaveable { mutableStateOf<PreviewRole?>(null) }
    val customerState = rememberSaveableStateHolder()
    val showRolePicker = { role = null }

    BackHandler(enabled = role != null, onBack = showRolePicker)

    when (role) {
        PreviewRole.Customer -> customerState.SaveableStateProvider(PreviewRole.Customer.name) {
            CustomerPreviewApp()
        }
        PreviewRole.Owner -> when (val access = ownerSession.access) {
            is OwnerAccess.Granted -> OwnerPreviewApp(
                accessToken = access.accessToken,
                onAccessError = ownerSession::onOwnerAccessError,
                onClose = showRolePicker,
            )
            else -> OwnerLoginScreen(
                access = access,
                kakaoConfigured = kakaoConfigured,
                onSignIn = onSignIn,
                onRetry = ownerSession::enterOwner,
                onClose = showRolePicker,
            )
        }
        null -> CustomerTheme {
            Surface(Modifier.fillMaxSize()) {
                Box(Modifier.safeDrawingPadding().fillMaxSize().padding(24.dp), Alignment.Center) {
                    Column(Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("LastEat", style = MaterialTheme.typography.headlineLarge)
                        Text("어떤 역할로 시작할까요?", style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = { role = PreviewRole.Customer }, modifier = Modifier.fillMaxWidth()) {
                            Text("손님으로 시작")
                        }
                        OutlinedButton(
                            onClick = {
                                ownerSession.enterOwner()
                                role = PreviewRole.Owner
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("사장님으로 시작")
                        }
                    }
                }
            }
        }
    }
}
