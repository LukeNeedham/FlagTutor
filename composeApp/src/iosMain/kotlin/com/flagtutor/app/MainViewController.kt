package com.flagtutor.app

import androidx.compose.ui.window.ComposeUIViewController
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import com.flagtutor.app.di.iosModule
import com.flagtutor.app.ui.navigation.NavGraph
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    CrashRepositoryImpl().installUncaughtExceptionHandler()
    return ComposeUIViewController {
        App(extraModules = listOf(iosModule())) {
            NavGraph()
        }
    }
}
