package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable

/** The app's own debug page. It has no ViewModel: its tools are links to other pages. */
@Composable
fun AppDebugPage(
    onOpenSdkDebug: () -> Unit,
    onCountriesOverviewClick: () -> Unit,
    onExcludedCountriesClick: () -> Unit,
    onFlagAttemptsClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    AppDebugPageContent(
        onOpenSdkDebug = onOpenSdkDebug,
        onCountriesOverviewClick = onCountriesOverviewClick,
        onExcludedCountriesClick = onExcludedCountriesClick,
        onFlagAttemptsClick = onFlagAttemptsClick,
        onBackClick = onBackClick,
    )
}
