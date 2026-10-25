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

object HomeNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        HomePage(
            onNavigateToPickCountryNameGame = { navigator.push(PickCountryNameGameNavKey) },
            onNavigateToCredits = { navigator.push(CreditsNavKey) },
            onNavigateToSettings = { navigator.push(SettingsNavKey) },
            onNavigateToDebug = if (isDebugBuild) {
                { navigator.push(AppDebugNavKey) }
            } else {
                null
            },
        )
    }
}

object PickCountryNameGameNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        PickCountryNameGamePage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

/** Debug only: the game with [alpha2Code] as the first flag. Has no browser route. */
data class DebugPickCountryNameGameNavKey(val alpha2Code: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        PickCountryNameGamePage(
            onNavigateBack = { navigator.pop() },
            forcedAlpha2Code = alpha2Code,
        )
    }
}

object CreditsNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CreditsPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object SettingsNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SettingsPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

/** The app's own debug page. It links to [DebugNavKey], the shared library's debug page. */
object AppDebugNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AppDebugPage(
            onOpenSdkDebug = { navigator.push(DebugNavKey) },
            onCountriesOverviewClick = { navigator.push(CountriesOverviewNavKey) },
            onExcludedCountriesClick = { navigator.push(ExcludedCountriesNavKey) },
            onFlagAttemptsClick = { navigator.push(FlagAttemptsNavKey) },
            onBackClick = { navigator.pop() },
        )
    }
}

/** The shared library's debug page: animation speed, and links to its logs and crashes pages. */
object DebugNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DebugPage(
            onBack = { navigator.pop() },
            onOpenLogs = { navigator.push(LogsNavKey) },
            onOpenCrashes = { navigator.push(CrashesNavKey) },
        )
    }
}

object CountriesOverviewNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CountriesOverviewPage(
            onNavigateBack = { navigator.pop() },
            onNavigateToGame = { code -> navigator.push(DebugPickCountryNameGameNavKey(code)) },
        )
    }
}

object ExcludedCountriesNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ExcludedCountriesPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object FlagAttemptsNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        FlagAttemptsPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object LogsNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LogsPage(onBack = { navigator.pop() })
    }
}

object CrashesNavKey : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CrashesPage(onBack = { navigator.pop() })
    }
}
