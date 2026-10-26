package com.flagtutor.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.flagtutor.app.di.webModule
import com.flagtutor.app.ui.navigation.AppNavigation

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initDebugKit()
    ComposeViewport {
        App(extraModules = listOf(webModule())) {
            AppNavigation(
                initialRoutes = BrowserRoutes.initialRoutes(),
                // The browser's back button pops the back stack through the history sync, so this must not pop it too
                onBack = {},
                backStackHook = { backStack -> BrowserNavigationSync(backStack) },
            )
        }
    }
}
