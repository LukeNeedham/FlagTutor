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
            // NavDisplay's entryProvider is typed against the generic NavKey marker interface
            // rather than Destination, so cast once here to get an exhaustive `when` below:
            // a new Destination subtype without a matching branch becomes a compile error again.
            when (val dest = destination as Destination) {
                Destination.Home -> NavEntry(dest) {
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

                Destination.PickCountryNameGame -> NavEntry(dest) {
                    PickCountryNameGamePage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.Credits -> NavEntry(dest) {
                    CreditsPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.Debug -> NavEntry(dest) {
                    DebugPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToCountriesOverview = { backStack.add(Destination.CountriesOverview) },
                        onNavigateToFlagAttempts = { backStack.add(Destination.FlagAttempts) },
                        onNavigateToCrashes = { backStack.add(Destination.Crashes) },
                    )
                }

                Destination.CountriesOverview -> NavEntry(dest) {
                    CountriesOverviewPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.FlagAttempts -> NavEntry(dest) {
                    FlagAttemptsPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                Destination.Crashes -> NavEntry(dest) {
                    CrashesPage(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToCrashDetail = { crash -> backStack.add(Destination.CrashDetail(crash)) },
                    )
                }

                is Destination.CrashDetail -> NavEntry(dest) {
                    CrashDetailPage(
                        crash = dest.crash,
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }
            }
        },
    )
}
