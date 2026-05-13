package com.tony.coreui.presentation.components.basescreen

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Synchronizes system bar colors and icon appearance with the active screen styling.
 */
@Composable
@Suppress("DEPRECATION")
fun SystemAppearance(
    statusBarColor: Color,
    navigationBarColor: Color = statusBarColor,
    useLightStatusIcons: Boolean? = null,
    useLightNavigationIcons: Boolean? = null
) {
    val view = LocalView.current
    if (view.isInEditMode) {
        return
    }

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)

        val useDarkStatusIcons = useLightStatusIcons?.not() ?: (statusBarColor.luminance() > 0.5f)
        val useDarkNavigationIcons =
            useLightNavigationIcons?.not() ?: (navigationBarColor.luminance() > 0.5f)

        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()
        controller.isAppearanceLightStatusBars = useDarkStatusIcons
        controller.isAppearanceLightNavigationBars = useDarkNavigationIcons
    }
}
