package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PickCountryNameGamePage(
    onNavigateBack: () -> Unit,
    viewModel: PickCountryNameGameViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    // The view model can outlive the page (e.g. on web), so game state is reset explicitly.
    DisposableEffect(viewModel) {
        viewModel.onPageShown()
        onDispose { viewModel.onPageLeft() }
    }
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
