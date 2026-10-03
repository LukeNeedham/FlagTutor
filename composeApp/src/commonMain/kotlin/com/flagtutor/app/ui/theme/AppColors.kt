package com.flagtutor.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** App-specific colours that have no slot in the Material [androidx.compose.material3.ColorScheme]. */
data class AppColors(
    val logoBackground: Color,
)

internal val LightAppColors = AppColors(logoBackground = LightLogoBackground)
internal val DarkAppColors = AppColors(logoBackground = DarkLogoBackground)

internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
