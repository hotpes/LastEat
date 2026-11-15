package com.lasteat.app.ui.customer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal object CustomerColors {
    val Green = Color(0xFF26734D)
    val DeepGreen = Color(0xFF174D35)
    val LightGreen = Color(0xFFEAF3E9)
    val Ink = Color(0xFF202A25)
    val Muted = Color(0xFF778078)
    val Body = Color(0xFF535F56)
    val Line = Color(0xFFE8EBE5)
    val Section = Color(0xFFF7F8F5)
}

@Composable
internal fun CustomerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = CustomerColors.Green, onPrimary = Color.White,
        primaryContainer = CustomerColors.LightGreen,
        onPrimaryContainer = CustomerColors.DeepGreen,
        background = Color.White, surface = Color.White,
        onSurface = CustomerColors.Ink, onSurfaceVariant = CustomerColors.Muted,
        outlineVariant = CustomerColors.Line,
    ), content = content)
}
