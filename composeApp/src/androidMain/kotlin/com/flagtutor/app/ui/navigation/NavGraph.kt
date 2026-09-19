package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.flagtutor.app.BuildConfig
import com.flagtutor.app.data.crash.CrashEntry
import com.flagtutor.app.ui.feature.crashes.CrashDetailPage
import com.flagtutor.app.ui.feature.crashes.CrashesPage
import com.flagtutor.app.ui.feature.credits.CreditsPage
import com.flagtutor.app.ui.feature.debug.CountriesOverviewPage
import com.flagtutor.app.ui.feature.debug.DebugPage
import com.flagtutor.app.ui.feature.flagattempts.FlagAttemptsPage
import com.flagtutor.app.ui.feature.home.HomePage
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGamePage
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.NavHost
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController

@Composable
fun NavGraph() {
    val navController = rememberNavController<Destination>(startDestination = Destination.Home)
    var selectedCrash by remember { mutableStateOf<CrashEntry?>(null) }

    NavBackHandler(navController)

    NavHost(navController) { destination ->
        when (destination) {
            Destination.Home -> HomePage(
                onNavigateToPickCountryNameGame = { navController.navigate(Destination.PickCountryNameGame) },
                onNavigateToCredits = { navController.navigate(Destination.Credits) },
                onNavigateToDebug = if (BuildConfig.DEBUG) {
                    { navController.navigate(Destination.Debug) }
                } else {
                    null
                },
            )

            Destination.PickCountryNameGame -> PickCountryNameGamePage(
                onNavigateBack = { navController.pop() },
            )

            Destination.Credits -> CreditsPage(
                onNavigateBack = { navController.pop() },
            )

            Destination.Debug -> DebugPage(
                onNavigateBack = { navController.pop() },
                onNavigateToCountriesOverview = { navController.navigate(Destination.CountriesOverview) },
                onNavigateToFlagAttempts = { navController.navigate(Destination.FlagAttempts) },
                onNavigateToCrashes = { navController.navigate(Destination.Crashes) },
            )

            Destination.CountriesOverview -> CountriesOverviewPage(
                onNavigateBack = { navController.pop() },
            )

            Destination.FlagAttempts -> FlagAttemptsPage(
                onNavigateBack = { navController.pop() },
            )

            Destination.Crashes -> CrashesPage(
                onNavigateBack = { navController.pop() },
                onNavigateToCrashDetail = { crash ->
                    selectedCrash = crash
                    navController.navigate(Destination.CrashDetail)
                },
            )

            Destination.CrashDetail -> {
                val crash = selectedCrash
                if (crash != null) {
                    CrashDetailPage(
                        crash = crash,
                        onNavigateBack = { navController.pop() },
                    )
                } else {
                    navController.pop()
                }
            }
        }
    }
}
