package com.flagtutor.app.ui.feature.game

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GamePage(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = koinViewModel(),
) {
    GamePageContent(
        uiState = viewModel.uiState,
        onOptionSelected = viewModel::onOptionSelected,
        onNextFlag = viewModel::onNextFlag,
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
