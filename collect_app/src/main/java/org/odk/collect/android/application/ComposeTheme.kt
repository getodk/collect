package org.odk.collect.android.application

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.odk.collect.qrcode.mlkit.ComposeThemeProvider

@Composable
fun CollectTheme(
    content: @Composable() () -> Unit
) {
    val lightColors = lightColorScheme(surface = Color(0xFFFFFFFF))
    val darkColors = darkColorScheme(surface = Color(0xFF001117))
    val colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

interface CollectComposeThemeProvider : ComposeThemeProvider {
    @Composable
    override fun Theme(content: @Composable (() -> Unit)) {
        CollectTheme { content() }
    }
}
