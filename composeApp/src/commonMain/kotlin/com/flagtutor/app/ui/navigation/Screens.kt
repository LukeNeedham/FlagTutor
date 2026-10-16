package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.flagtutor.app.isDebugBuild
import com.flagtutor.app.ui.feature.credits.CreditsPage
import com.flagtutor.app.ui.feature.debug.CountriesOverviewPage
import com.flagtutor.app.ui.feature.debug.ExcludedCountriesPage
import com.flagtutor.app.ui.feature.debug.AppDebugLinks
import com.flagtutor.app.ui.feature.flagattempts.FlagAttemptsPage
import com.flagtutor.app.ui.feature.home.HomePage
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGamePage
import com.flagtutor.app.ui.feature.settings.SettingsPage
import com.lukeneedham.common.ui.CrashesPage
import com.lukeneedham.common.ui.DebugPage
import com.lukeneedham.common.ui.LogsPage

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        HomePage(
            onNavigateToPickCountryNameGame = { navigator.push(PickCountryNameGameScreen) },
            onNavigateToCredits = { navigator.push(CreditsScreen) },
            onNavigateToSettings = { navigator.push(SettingsScreen) },
            onNavigateToDebug = if (isDebugBuild) {
                { navigator.push(DebugScreen) }
            } else {
                null
            },
        )
    }
}

object PickCountryNameGameScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        PickCountryNameGamePage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

/** Debug only: the game with [alpha2Code] as the first flag. Has no browser route. */
data class DebugPickCountryNameGameScreen(val alpha2Code: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        PickCountryNameGamePage(
            onNavigateBack = { navigator.pop() },
            forcedAlpha2Code = alpha2Code,
        )
    }
}

object CreditsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CreditsPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SettingsPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object DebugScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DebugPage(
            onBack = { navigator.pop() },
            onOpenLogs = { navigator.push(LogsScreen) },
            onOpenCrashes = { navigator.push(CrashesScreen) },
        ) {
            AppDebugLinks(
                onCountriesOverviewClick = { navigator.push(CountriesOverviewScreen) },
                onExcludedCountriesClick = { navigator.push(ExcludedCountriesScreen) },
                onFlagAttemptsClick = { navigator.push(FlagAttemptsScreen) },
            )
        }
    }
}

object CountriesOverviewScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CountriesOverviewPage(
            onNavigateBack = { navigator.pop() },
            onNavigateToGame = { code -> navigator.push(DebugPickCountryNameGameScreen(code)) },
        )
    }
}

object ExcludedCountriesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ExcludedCountriesPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object FlagAttemptsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        FlagAttemptsPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object LogsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LogsPage(onBack = { navigator.pop() })
    }
}

object CrashesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CrashesPage(onBack = { navigator.pop() })
    }
}
