package com.flagtutor.app.ui.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GamePage(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    GamePageContent(
        uiState = viewModel.uiState,
        onOptionSelected = viewModel::onOptionSelected,
        onNextFlag = viewModel::onNextFlag,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
