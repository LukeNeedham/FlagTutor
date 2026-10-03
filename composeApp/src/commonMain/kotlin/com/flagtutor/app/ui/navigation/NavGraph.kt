package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.jetpack.ProvideNavigatorLifecycleKMPSupport
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
    // Gives every screen its own ViewModelStoreOwner, so view models are cleared when the screen
    // is popped (otherwise they would live as long as the whole app, e.g. on web).
    ProvideNavigatorLifecycleKMPSupport {
        Navigator(initialScreens) { navigator ->
            navigatorHook(navigator)
            CurrentScreen()
        }
    }
}
