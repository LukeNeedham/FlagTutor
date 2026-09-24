package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.flagtutor.app.isDebugBuild
import com.flagtutor.app.ui.feature.crashes.CrashDetailPage
import com.flagtutor.app.ui.feature.crashes.CrashesPage
import com.flagtutor.app.ui.feature.credits.CreditsPage
import com.flagtutor.app.ui.feature.debug.CountriesOverviewPage
import com.flagtutor.app.ui.feature.debug.DebugPage
import com.flagtutor.app.ui.feature.flagattempts.FlagAttemptsPage
import com.flagtutor.app.ui.feature.home.HomePage
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGamePage

@Composable
fun NavGraph() {
    val backStack = rememberNavBackStack(Destination.Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { destination ->
            when (destination) {
                Destination.Home -> NavEntry(destination) {
                    HomePage(
                        onNavigateToPickCountryNameGame = { backStack.add(Destination.PickCountryNameGame) },
                        onNavigateToCredits = { backStack.add(Destination.Credits) },
                        onNavigateToDebug = if (isDebugBuild) {
                            { backStack.add(Destination.Debug) }
                        } else {
                            null
                        },
                    )
                }

                Destination.PickCountryNameGame -> NavEntry(destination) {
                    PickCountryNameGamePage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.Credits -> NavEntry(destination) {
                    CreditsPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.Debug -> NavEntry(destination) {
                    DebugPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToCountriesOverview = { backStack.add(Destination.CountriesOverview) },
                        onNavigateToFlagAttempts = { backStack.add(Destination.FlagAttempts) },
                        onNavigateToCrashes = { backStack.add(Destination.Crashes) },
                    )
                }

                Destination.CountriesOverview -> NavEntry(destination) {
                    CountriesOverviewPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.FlagAttempts -> NavEntry(destination) {
                    FlagAttemptsPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.Crashes -> NavEntry(destination) {
                    CrashesPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToCrashDetail = { crash -> backStack.add(Destination.CrashDetail(crash)) },
                    )
                }

                is Destination.CrashDetail -> NavEntry(destination) {
                    CrashDetailPage(
                        crash = destination.crash,
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }
            }
        },
    )
}
