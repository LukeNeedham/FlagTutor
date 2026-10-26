package com.flagtutor.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Saving the back stack needs every route registered: iOS and the web have no reflection to find them. A new [Route]
 * member is added here too.
 */
private val backStackConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Home::class, Route.Home.serializer())
            subclass(Route.PickCountryNameGame::class, Route.PickCountryNameGame.serializer())
            subclass(Route.DebugPickCountryNameGame::class, Route.DebugPickCountryNameGame.serializer())
            subclass(Route.Credits::class, Route.Credits.serializer())
            subclass(Route.Settings::class, Route.Settings.serializer())
            subclass(Route.AppDebug::class, Route.AppDebug.serializer())
            subclass(Route.Debug::class, Route.Debug.serializer())
            subclass(Route.CountriesOverview::class, Route.CountriesOverview.serializer())
            subclass(Route.ExcludedCountries::class, Route.ExcludedCountries.serializer())
            subclass(Route.FlagAttempts::class, Route.FlagAttempts.serializer())
            subclass(Route.Logs::class, Route.Logs.serializer())
            subclass(Route.Crashes::class, Route.Crashes.serializer())
        }
    }
}

/**
 * Owns the back stack and the [NavDisplay]. Called only by the app's root.
 *
 * @param initialRoutes the back stack to start with, bottom first (e.g. to open a deep link).
 * @param onBack called when the system's back is used; by default it pops the back stack. The web passes a function that
 * does nothing, because there the browser's back button is handled by the history sync.
 * @param backStackHook called with the back stack on every composition, for platform code that needs to observe or
 * drive navigation (e.g. keeping the browser's history in sync).
 */
@Composable
fun AppNavigation(
    initialRoutes: List<Route> = listOf(Route.Home),
    onBack: (NavBackStack<NavKey>) -> Unit = { it.removeLastOrNull() },
    backStackHook: @Composable (NavBackStack<NavKey>) -> Unit = {},
) {
    val backStack = rememberNavBackStack(backStackConfig, *initialRoutes.toTypedArray())
    backStackHook(backStack)

    // Pages swap without an animation
    val noTransition = EnterTransition.None togetherWith ExitTransition.None
    NavDisplay(
        backStack = backStack,
        // Each page has a view model of its own, kept for as long as the page is on the back stack
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { onBack(backStack) },
        transitionSpec = { noTransition },
        popTransitionSpec = { noTransition },
        predictivePopTransitionSpec = { noTransition },
        entryProvider = { key -> routeEntryProvider(backStack, key) },
    )
}

private fun routeEntryProvider(backStack: NavBackStack<NavKey>, key: NavKey): NavEntry<NavKey> {
    val route = key as Route
    return NavEntry(route) {
        Router(backStack, route)
    }
}
