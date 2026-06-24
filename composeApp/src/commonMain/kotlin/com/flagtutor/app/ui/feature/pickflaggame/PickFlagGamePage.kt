package com.flagtutor.app.ui.feature.pickflaggame

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGameViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PickFlagGamePage(
    onNavigateBack: () -> Unit,
    viewModel: PickCountryNameGameViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    PickFlagGamePageContent(
        uiState = viewModel.uiState,
        onOptionSelected = viewModel::onOptionSelected,
        onNextFlag = viewModel::onNextFlag,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
