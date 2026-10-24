package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.core.parameter.parametersOf
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PickCountryNameGamePage(
    onNavigateBack: () -> Unit,
    forcedAlpha2Code: String? = null,
    viewModel: PickCountryNameGameViewModel = koinViewModel(key = forcedAlpha2Code) {
        parametersOf(forcedAlpha2Code)
    },
) {
    val uriHandler = LocalUriHandler.current
    PickCountryNameGamePageContent(
        uiState = viewModel.uiState,
        onOptionSelected = viewModel::onOptionSelected,
        onNextFlag = viewModel::onNextFlag,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onOpenMap = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
