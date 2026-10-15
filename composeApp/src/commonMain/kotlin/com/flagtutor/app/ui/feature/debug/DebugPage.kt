package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable

@Composable
fun DebugPage(
    onNavigateBack: () -> Unit,
    onNavigateToCountriesOverview: () -> Unit,
    onNavigateToExcludedCountries: () -> Unit,
    onNavigateToFlagAttempts: () -> Unit,
    onNavigateToCrashes: () -> Unit,
) {
    DebugPageContent(
        onCountriesOverviewClick = onNavigateToCountriesOverview,
        onExcludedCountriesClick = onNavigateToExcludedCountries,
        onFlagAttemptsClick = onNavigateToFlagAttempts,
        onCrashesClick = onNavigateToCrashes,
        onBackClick = onNavigateBack,
    )
}
