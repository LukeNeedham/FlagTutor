package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugPage(
    onNavigateBack: () -> Unit,
    viewModel: DebugViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    DebugPageContent(
        countries = viewModel.countries,
        isLoading = viewModel.isLoading,
        isError = viewModel.isError,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
