package com.flagtutor.app.ui.feature.flagattempts

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FlagAttemptsPage(
    onNavigateBack: () -> Unit,
    viewModel: FlagAttemptsViewModel = koinViewModel(),
) {
    FlagAttemptsPageContent(
        attempts = viewModel.attempts,
        isLoading = viewModel.isLoading,
        onBackClick = onNavigateBack,
    )
}
