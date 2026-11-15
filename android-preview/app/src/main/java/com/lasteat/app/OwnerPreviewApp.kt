package com.lasteat.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.lasteat.app.ui.owner.OwnerReadApi
import com.lasteat.app.ui.owner.OwnerReadException
import com.lasteat.app.ui.owner.OwnerScreenFromServer

@Composable
internal fun OwnerPreviewApp(
    accessToken: String,
    onAccessError: (OwnerReadException) -> Boolean,
    onClose: () -> Unit,
) {
    val api = remember { OwnerReadApi() }
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF246448), onPrimary = Color.White,
        primaryContainer = Color(0xFFE6F1E9), onPrimaryContainer = Color(0xFF173E2C),
        surface = Color.White, surfaceVariant = Color(0xFFF5F7F4),
        onSurface = Color(0xFF1D2923), onSurfaceVariant = Color(0xFF65716A),
        outlineVariant = Color(0xFFE1E7E2),
    )) {
        OwnerScreenFromServer(
            api = api,
            accessToken = accessToken,
            onClose = onClose,
            onAccessError = onAccessError,
        )
    }
}
