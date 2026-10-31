package com.flagtutor.app

import androidx.compose.ui.window.ComposeUIViewController
import com.flagtutor.app.di.IosModule
import com.flagtutor.app.ui.navigation.AppNavigation
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initDebugKit()
    return ComposeUIViewController {
        App(extraModules = listOf(IosModule.bindings())) {
            AppNavigation()
        }
    }
}
