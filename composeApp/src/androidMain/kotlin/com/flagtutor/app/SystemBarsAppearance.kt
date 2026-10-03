package com.flagtutor.app

import android.app.Activity
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * The app theme is chosen in-app rather than by the system, so the status/navigation bar icons
 * must follow it: dark icons on a light background, light icons on a dark one.
 */
@Composable
fun SystemBarsAppearance() {
    val view = LocalView.current
    val isLight = AppTheme.colors.background.luminance() > 0.5f
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = isLight
            isAppearanceLightNavigationBars = isLight
        }
    }
}
