package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable

@Composable
fun DebugPage(
    onNavigateBack: () -> Unit,
    onNavigateToCountriesOverview: () -> Unit,
    onNavigateToCountriesWithoutFlag: () -> Unit,
    onNavigateToFlagAttempts: () -> Unit,
    onNavigateToCrashes: () -> Unit,
) {
    DebugPageContent(
        onCountriesOverviewClick = onNavigateToCountriesOverview,
        onCountriesWithoutFlagClick = onNavigateToCountriesWithoutFlag,
        onFlagAttemptsClick = onNavigateToFlagAttempts,
        onCrashesClick = onNavigateToCrashes,
        onBackClick = onNavigateBack,
    )
}
