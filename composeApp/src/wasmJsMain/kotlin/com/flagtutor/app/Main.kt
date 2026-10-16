package com.flagtutor.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.flagtutor.app.di.webModule
import com.flagtutor.app.ui.navigation.NavGraph

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initDebugKit()
    ComposeViewport {
        App(extraModules = listOf(webModule())) {
            NavGraph(
                initialScreens = BrowserRoutes.initialScreens(),
                navigatorHook = { navigator -> BrowserNavigationSync(navigator) },
            )
        }
    }
}
