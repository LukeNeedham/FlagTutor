package com.flagtutor.app

import androidx.compose.runtime.Composable
import com.flagtutor.app.di.appModule
import com.flagtutor.app.ui.theme.FlagTutorTheme
import org.koin.compose.KoinApplication

@Composable
fun App(content: @Composable () -> Unit) {
    KoinApplication(application = { modules(appModule) }) {
        FlagTutorTheme {
            content()
        }
    }
}
