package com.flagtutor.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.flagtutor.app.data.settings.DebugSettings
import com.flagtutor.app.data.settings.ThemeRepository
import com.flagtutor.app.ui.util.LocalScaledAnimation
import com.flagtutor.app.ui.util.ScaledAnimation
import com.flagtutor.app.di.appModule
import com.flagtutor.app.ui.theme.FlagTutorTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

@Composable
fun App(extraModules: List<Module> = emptyList(), content: @Composable () -> Unit) {
    KoinApplication(application = { modules(listOf(appModule) + extraModules) }) {
        val themeRepository = koinInject<ThemeRepository>()
        val debugSettings = koinInject<DebugSettings>()
        FlagTutorTheme(themeMode = themeRepository.themeMode) {
            val scaledAnimation = remember(debugSettings.animationSpeed) {
                ScaledAnimation(debugSettings.animationSpeed.multiplier)
            }
            CompositionLocalProvider(LocalScaledAnimation provides scaledAnimation) {
                content()
            }
        }
    }
}
