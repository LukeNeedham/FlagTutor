package com.flagtutor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.flagtutor.app.ui.navigation.NavGraph
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module

private const val SETTINGS_NAME = "flag_tutor_settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageLoader = ImageLoader.Builder(applicationContext)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()

        val platformModule = module {
            single<PlatformContext> { applicationContext }
            single { imageLoader }
            single<Settings> {
                SharedPreferencesSettings(getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE))
            }
        }

        setContent {
            App(platformModule = platformModule) {
                NavGraph()
            }
        }
    }
}
