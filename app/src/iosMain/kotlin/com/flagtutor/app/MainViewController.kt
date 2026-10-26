package com.flagtutor.app

import androidx.compose.ui.window.ComposeUIViewController
import com.flagtutor.app.di.iosModule
import com.flagtutor.app.ui.navigation.AppNavigation
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initDebugKit()
    return ComposeUIViewController {
        App(extraModules = listOf(iosModule())) {
            AppNavigation()
        }
    }
}
