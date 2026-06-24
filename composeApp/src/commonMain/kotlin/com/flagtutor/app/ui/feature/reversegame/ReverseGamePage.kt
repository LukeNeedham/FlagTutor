package com.flagtutor.app.ui.feature.reversegame

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.flagtutor.app.ui.feature.game.GameViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReverseGamePage(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    ReverseGamePageContent(
        uiState = viewModel.uiState,
        onOptionSelected = viewModel::onOptionSelected,
        onNextFlag = viewModel::onNextFlag,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
