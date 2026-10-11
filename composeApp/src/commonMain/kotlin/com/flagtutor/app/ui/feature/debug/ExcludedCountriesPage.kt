package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExcludedCountriesPage(
    onNavigateBack: () -> Unit,
    viewModel: ExcludedCountriesViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    ExcludedCountriesPageContent(
        countries = viewModel.countries,
        isLoading = viewModel.isLoading,
        isError = viewModel.isError,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
