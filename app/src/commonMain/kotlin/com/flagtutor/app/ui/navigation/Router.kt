package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.flagtutor.app.isDebugBuild
import com.flagtutor.app.ui.feature.credits.CreditsPage
import com.flagtutor.app.ui.feature.debug.AppDebugPage
import com.flagtutor.app.ui.feature.debug.CountriesOverviewPage
import com.flagtutor.app.ui.feature.debug.ExcludedCountriesPage
import com.flagtutor.app.ui.feature.flagattempts.FlagAttemptsPage
import com.flagtutor.app.ui.feature.home.HomePage
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGamePage
import com.flagtutor.app.ui.feature.settings.SettingsPage
import com.lukeneedham.common.ui.CrashesPage
import com.lukeneedham.common.ui.DebugPage
import com.lukeneedham.common.ui.LogsPage

/** Calls the page of [route], with the navigation as lambdas. The only place that calls a page. */
@Composable
fun Router(backStack: NavBackStack<NavKey>, route: Route) {
    // The common actions are defined once here, never repeated in each branch
    fun navigateTo(target: Route) { backStack.add(target) }
    fun onBack() { backStack.removeLastOrNull() }

    when (route) { // exhaustive: no `else`
        Route.Home -> HomePage(
            onNavigateToPickCountryNameGame = { navigateTo(Route.PickCountryNameGame) },
            onNavigateToCredits = { navigateTo(Route.Credits) },
            onNavigateToSettings = { navigateTo(Route.Settings) },
            onNavigateToDebug = if (isDebugBuild) {
                { navigateTo(Route.AppDebug) }
            } else {
                null
            },
        )

        Route.PickCountryNameGame -> PickCountryNameGamePage(
            onNavigateBack = ::onBack,
        )

        is Route.DebugPickCountryNameGame -> PickCountryNameGamePage(
            onNavigateBack = ::onBack,
            forcedAlpha2Code = route.alpha2Code,
        )

        Route.Credits -> CreditsPage(
            onNavigateBack = ::onBack,
        )

        Route.Settings -> SettingsPage(
            onNavigateBack = ::onBack,
        )

        Route.AppDebug -> AppDebugPage(
            onOpenSdkDebug = { navigateTo(Route.Debug) },
            onCountriesOverviewClick = { navigateTo(Route.CountriesOverview) },
            onExcludedCountriesClick = { navigateTo(Route.ExcludedCountries) },
            onFlagAttemptsClick = { navigateTo(Route.FlagAttempts) },
            onBackClick = ::onBack,
        )

        Route.Debug -> DebugPage(
            onBack = ::onBack,
            onOpenLogs = { navigateTo(Route.Logs) },
            onOpenCrashes = { navigateTo(Route.Crashes) },
        )

        Route.CountriesOverview -> CountriesOverviewPage(
            onNavigateBack = ::onBack,
            onNavigateToGame = { code -> navigateTo(Route.DebugPickCountryNameGame(code)) },
        )

        Route.ExcludedCountries -> ExcludedCountriesPage(
            onNavigateBack = ::onBack,
        )

        Route.FlagAttempts -> FlagAttemptsPage(
            onNavigateBack = ::onBack,
        )

        Route.Logs -> LogsPage(onBack = ::onBack)

        Route.Crashes -> CrashesPage(onBack = ::onBack)
    }
}
