package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator

/**
 * @param initialScreens the back stack to start with, bottom first (e.g. to open a deep link).
 * @param navigatorHook called with the [Navigator] on every composition, for platform code that
 * needs to observe or drive navigation (e.g. keeping the browser's history in sync).
 */
@Composable
fun NavGraph(
    initialScreens: List<Screen> = listOf(HomeScreen),
    navigatorHook: @Composable (Navigator) -> Unit = {},
) {
    Navigator(initialScreens) { navigator ->
        navigatorHook(navigator)
        CurrentScreen()
    }
}
