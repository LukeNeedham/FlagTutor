package com.flagtutor.app.ui.feature.home

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomePage(
    onNavigateToPickCountryNameGame: () -> Unit,
    onNavigateToPickFlagGame: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDebug: (() -> Unit)? = null,
    viewModel: HomeViewModel = koinViewModel(),
) {
    HomePageContent(
        title = viewModel.title,
        subtitle = viewModel.subtitle,
        onGuessCountryClick = onNavigateToPickCountryNameGame,
        onGuessTheFlagClick = onNavigateToPickFlagGame,
        onAboutClick = onNavigateToAbout,
        onDebugClick = onNavigateToDebug,
    )
}
