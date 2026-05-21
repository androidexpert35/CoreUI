package com.tony.coreui.sample.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF0B1F52),
    secondary = Color(0xFF0F766E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1FAF5),
    onSecondaryContainer = Color(0xFF113431),
    tertiary = Color(0xFF9A3412),
    background = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EEF8)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DBBFF),
    onPrimary = Color(0xFF032864),
    primaryContainer = Color(0xFF183A85),
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = Color(0xFF76D6CB),
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF11514B),
    onSecondaryContainer = Color(0xFFD1FAF5),
    tertiary = Color(0xFFF6A47F),
    background = Color(0xFF111318),
    surface = Color(0xFF181C22),
    surfaceVariant = Color(0xFF202731)
)

/** Material theme used by the sample app's Compose screens. */
@Composable
fun CoreUiSampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
