package com.flagtutor.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.flagtutor.app.data.settings.DebugSettings
import com.flagtutor.app.data.settings.ThemeRepository
import com.flagtutor.app.ui.util.LocalAnimationSpeed
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
            CompositionLocalProvider(LocalAnimationSpeed provides debugSettings.animationSpeed.multiplier) {
                content()
            }
        }
    }
}
