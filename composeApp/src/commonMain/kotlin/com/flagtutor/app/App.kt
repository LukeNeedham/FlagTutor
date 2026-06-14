package com.flagtutor.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.flagtutor.app.di.appModule
import org.koin.compose.KoinApplication

@Composable
fun App(content: @Composable () -> Unit) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            content()
        }
    }
}
