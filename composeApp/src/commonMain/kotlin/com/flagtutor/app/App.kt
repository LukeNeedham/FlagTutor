package com.flagtutor.app

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.flagtutor.app.di.appModule
import com.flagtutor.app.ui.theme.FlagTutorTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

@Composable
fun App(platformModule: Module, content: @Composable () -> Unit) {
    KoinApplication(application = { modules(appModule, platformModule) }) {
        val imageLoader = koinInject<ImageLoader>()
        setSingletonImageLoaderFactory { imageLoader }

        FlagTutorTheme {
            content()
        }
    }
}
