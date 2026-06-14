package com.flagtutor.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.flagtutor.app.di.appModule
import org.koin.compose.KoinApplication

@Composable
fun App(content: @Composable () -> Unit) {
    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            content()
        }
    }
}
