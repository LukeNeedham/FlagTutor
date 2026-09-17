package com.flagtutor.app.ui.feature.home

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomePage(
    onNavigateToPickCountryNameGame: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onNavigateToDebug: (() -> Unit)? = null,
    viewModel: HomeViewModel = koinViewModel(),
) {
    HomePageContent(
        title = viewModel.title,
        subtitle = viewModel.subtitle,
        onGuessCountryClick = onNavigateToPickCountryNameGame,
        onCreditsClick = onNavigateToCredits,
        onDebugClick = onNavigateToDebug,
    )
}
