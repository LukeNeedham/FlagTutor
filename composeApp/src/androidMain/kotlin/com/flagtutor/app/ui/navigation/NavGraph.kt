package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import com.flagtutor.app.BuildConfig
import com.flagtutor.app.ui.feature.about.AboutPage
import com.flagtutor.app.ui.feature.debug.DebugPage
import com.flagtutor.app.ui.feature.game.GamePage
import com.flagtutor.app.ui.feature.home.HomePage
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.NavHost
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController

@Composable
fun NavGraph() {
    val navController = rememberNavController<Destination>(startDestination = Destination.Home)

    NavBackHandler(navController)

    NavHost(navController) { destination ->
        when (destination) {
            Destination.Home -> HomePage(
                onNavigateToGame = { navController.navigate(Destination.Game) },
                onNavigateToAbout = { navController.navigate(Destination.About) },
                onNavigateToDebug = if (BuildConfig.DEBUG) {
                    { navController.navigate(Destination.Debug) }
                } else {
                    null
                },
            )

            Destination.About -> AboutPage(
                onNavigateBack = { navController.pop() },
            )

            Destination.Game -> GamePage(
                onNavigateBack = { navController.pop() },
            )

            Destination.Debug -> DebugPage(
                onNavigateBack = { navController.pop() },
            )
        }
    }
}
