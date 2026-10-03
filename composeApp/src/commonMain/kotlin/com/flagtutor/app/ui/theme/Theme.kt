package com.flagtutor.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.flagtutor.app.domain.model.ThemeMode

internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }
internal val LocalAppTypography = staticCompositionLocalOf { DefaultAppTypography }
internal val LocalAppShapes = staticCompositionLocalOf { DefaultAppShapes }

/** Entry point for reading the app's theme: `AppTheme.colors`, `AppTheme.typography`, `AppTheme.shapes`. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalAppShapes.current
}

@Composable
fun FlagTutorTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides DefaultAppTypography,
        LocalAppShapes provides DefaultAppShapes,
    ) {
        // Material components read MaterialTheme internally for their defaults (ripple, text selection,
        // etc.). Feed it from AppTheme so they stay consistent; app code must not read it directly.
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(darkTheme),
            shapes = DefaultAppShapes.toMaterialShapes(),
            content = content,
        )
    }
}

private fun AppColors.toMaterialColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        secondaryContainer = option,
        onSecondaryContainer = onOption,
        tertiary = correct,
        onTertiary = onCorrect,
        error = error,
        background = background,
        onBackground = onBackground,
        surface = background,
        onSurface = text,
        surfaceVariant = card,
        onSurfaceVariant = textSecondary,
        outlineVariant = divider,
    )
}

private fun AppShapes.toMaterialShapes() = Shapes(
    extraSmall = extraSmall,
    small = small,
    medium = medium,
    large = large,
    extraLarge = extraLarge,
)
