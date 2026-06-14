package com.flagtutor.app.ui.feature.home

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomePage(
    onNavigateToGame: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    HomePageContent(
        title = viewModel.title,
        subtitle = viewModel.subtitle,
        onStartClick = onNavigateToGame,
        onAboutClick = onNavigateToAbout,
    )
}
