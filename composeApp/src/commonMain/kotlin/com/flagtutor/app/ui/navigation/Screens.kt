package com.flagtutor.app.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.flagtutor.app.data.crash.CrashEntry
import com.flagtutor.app.isDebugBuild
import com.flagtutor.app.ui.feature.crashes.CrashDetailPage
import com.flagtutor.app.ui.feature.crashes.CrashesPage
import com.flagtutor.app.ui.feature.credits.CreditsPage
import com.flagtutor.app.ui.feature.debug.CountriesOverviewPage
import com.flagtutor.app.ui.feature.debug.DebugPage
import com.flagtutor.app.ui.feature.debugsettings.DebugSettingsPage
import com.flagtutor.app.ui.feature.flagattempts.FlagAttemptsPage
import com.flagtutor.app.ui.feature.home.HomePage
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGamePage
import com.flagtutor.app.ui.feature.settings.SettingsPage

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
            onNavigateBack = { navigator.pop() },
            onNavigateToCountriesOverview = { navigator.push(CountriesOverviewScreen) },
            onNavigateToFlagAttempts = { navigator.push(FlagAttemptsScreen) },
            onNavigateToCrashes = { navigator.push(CrashesScreen) },
            onNavigateToSettings = { navigator.push(DebugSettingsScreen) },
        )
    }
}

object DebugSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DebugSettingsPage(
            onNavigateBack = { navigator.pop() },
        )
    }
}

object CountriesOverviewScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CountriesOverviewPage(
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

object CrashesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CrashesPage(
            onNavigateBack = { navigator.pop() },
            onNavigateToCrashDetail = { crash -> navigator.push(CrashDetailScreen(crash)) },
        )
    }
}

data class CrashDetailScreen(val crash: CrashEntry) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CrashDetailPage(
            crash = crash,
            onNavigateBack = { navigator.pop() },
        )
    }
}
