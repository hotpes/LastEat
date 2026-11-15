package com.lasteat.app

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.SVG
import com.lasteat.app.ui.customer.CustomerTheme

@Composable
internal fun OwnerLoginScreen(
    access: OwnerAccess,
    kakaoConfigured: Boolean,
    onSignIn: () -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    CustomerTheme {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.safeDrawingPadding().padding(24.dp), Alignment.Center) {
                Column(
                    modifier = Modifier.widthIn(max = 360.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("사장님 로그인", style = MaterialTheme.typography.headlineMedium)
                    when (access) {
                        OwnerAccess.Checking -> CircularProgressIndicator()
                        OwnerAccess.TryAgain -> {
                            LoginMessage("서버에 연결하지 못했어요.")
                            Button(onClick = onRetry) { Text("다시 시도") }
                        }
                        OwnerAccess.Denied, is OwnerAccess.SignIn -> {
                            val message = when (access) {
                                OwnerAccess.Denied -> "사장님 권한이 있는 계정으로 로그인해 주세요."
                                is OwnerAccess.SignIn -> access.message
                                else -> null
                            }
                            if (message != null) LoginMessage(message)
                            if (kakaoConfigured) {
                                KakaoLoginButton(onSignIn)
                            } else {
                                LoginMessage("카카오 로그인 설정이 필요해요.")
                            }
                        }
                        is OwnerAccess.Granted -> Unit
                    }
                    TextButton(onClick = onClose) { Text("뒤로") }
                }
            }
        }
    }
}

@Composable
private fun LoginMessage(message: String) {
    Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun KakaoLoginButton(onClick: () -> Unit) {
    val assets = LocalContext.current.assets
    val picture = remember(assets) { SVG.getFromAsset(assets, "kakao_login_ko.svg").renderToPicture() }
    Box(
        modifier = Modifier.width(224.dp).height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "카카오 로그인" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.width(224.dp).height(46.dp)) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawPicture(picture, RectF(0f, 0f, size.width, size.height))
            }
        }
    }
}
