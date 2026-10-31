package com.flagtutor.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.flagtutor.app.data.repository.GamePreloader
import com.flagtutor.app.data.settings.ThemeRepository
import com.flagtutor.app.di.KoinModules
import com.flagtutor.app.ui.theme.AppTheme
import com.flagtutor.app.ui.theme.ProvideAppDebugSettings
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

@Composable
fun App(extraModules: List<Module> = emptyList(), content: @Composable () -> Unit) {
    KoinApplication(application = { modules(KoinModules.modules + extraModules) }) {
        val themeRepository = koinInject<ThemeRepository>()
        val gamePreloader = koinInject<GamePreloader>()
        LaunchedEffect(Unit) { gamePreloader.start() }
        AppTheme(themeMode = themeRepository.themeMode) {
            ProvideAppDebugSettings {
                content()
            }
        }
    }
}
