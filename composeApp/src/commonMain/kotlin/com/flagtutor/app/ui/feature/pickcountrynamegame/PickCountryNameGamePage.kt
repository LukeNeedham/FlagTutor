package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PickCountryNameGamePage(
    onNavigateBack: () -> Unit,
    viewModel: PickCountryNameGameViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    PickCountryNameGamePageContent(
        uiState = viewModel.uiState,
        onOptionSelected = viewModel::onOptionSelected,
        onNextFlag = viewModel::onNextFlag,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
