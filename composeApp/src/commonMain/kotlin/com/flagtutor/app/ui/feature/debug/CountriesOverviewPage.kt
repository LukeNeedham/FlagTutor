package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CountriesOverviewPage(
    onNavigateBack: () -> Unit,
    viewModel: CountriesOverviewViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    CountriesOverviewPageContent(
        countries = viewModel.countries,
        attemptStatsByCountry = viewModel.attemptStatsByCountry,
        isLoading = viewModel.isLoading,
        isError = viewModel.isError,
        onMoreInfo = { url -> uriHandler.openUri(url) },
        onRetry = viewModel::loadCountries,
        onBackClick = onNavigateBack,
    )
}
