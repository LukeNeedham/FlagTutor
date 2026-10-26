package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.flagtutor.app.isDebugBuild
import com.flagtutor.app.ui.feature.credits.CreditsPage
import com.flagtutor.app.ui.feature.debug.CountriesOverviewPage
import com.flagtutor.app.ui.feature.debug.ExcludedCountriesPage
import com.flagtutor.app.ui.feature.debug.AppDebugPage
import com.flagtutor.app.ui.feature.flagattempts.FlagAttemptsPage
import com.flagtutor.app.ui.feature.home.HomePage
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGamePage
import com.flagtutor.app.ui.feature.settings.SettingsPage
import com.lukeneedham.common.ui.CrashesPage
import com.lukeneedham.common.ui.DebugPage
import com.lukeneedham.common.ui.LogsPage

/** The destinations of the app, one per page: [Home] is `HomePage`, and so on. */
sealed interface Route : Screen {
    object Home : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            HomePage(
                onNavigateToPickCountryNameGame = { navigator.push(Route.PickCountryNameGame) },
                onNavigateToCredits = { navigator.push(Route.Credits) },
                onNavigateToSettings = { navigator.push(Route.Settings) },
                onNavigateToDebug = if (isDebugBuild) {
                    { navigator.push(Route.AppDebug) }
                } else {
                    null
                },
            )
        }
    }

    object PickCountryNameGame : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            PickCountryNameGamePage(
                onNavigateBack = { navigator.pop() },
            )
        }
    }

    /** Debug only: the game with [alpha2Code] as the first flag. Has no browser route. */
    data class DebugPickCountryNameGame(val alpha2Code: String) : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            PickCountryNameGamePage(
                onNavigateBack = { navigator.pop() },
                forcedAlpha2Code = alpha2Code,
            )
        }
    }

    object Credits : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            CreditsPage(
                onNavigateBack = { navigator.pop() },
            )
        }
    }

    object Settings : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            SettingsPage(
                onNavigateBack = { navigator.pop() },
            )
        }
    }

    /** The app's own debug page. It links to [Route.Debug], the shared library's debug page. */
    object AppDebug : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            AppDebugPage(
                onOpenSdkDebug = { navigator.push(Route.Debug) },
                onCountriesOverviewClick = { navigator.push(Route.CountriesOverview) },
                onExcludedCountriesClick = { navigator.push(Route.ExcludedCountries) },
                onFlagAttemptsClick = { navigator.push(Route.FlagAttempts) },
                onBackClick = { navigator.pop() },
            )
        }
    }

    /** The shared library's debug page: animation speed, and links to its logs and crashes pages. */
    object Debug : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            DebugPage(
                onBack = { navigator.pop() },
                onOpenLogs = { navigator.push(Route.Logs) },
                onOpenCrashes = { navigator.push(Route.Crashes) },
            )
        }
    }

    object CountriesOverview : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            CountriesOverviewPage(
                onNavigateBack = { navigator.pop() },
                onNavigateToGame = { code -> navigator.push(Route.DebugPickCountryNameGame(code)) },
            )
        }
    }

    object ExcludedCountries : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            ExcludedCountriesPage(
                onNavigateBack = { navigator.pop() },
            )
        }
    }

    object FlagAttempts : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            FlagAttemptsPage(
                onNavigateBack = { navigator.pop() },
            )
        }
    }

    object Logs : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            LogsPage(onBack = { navigator.pop() })
        }
    }

    object Crashes : Route {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            CrashesPage(onBack = { navigator.pop() })
        }
    }
}
