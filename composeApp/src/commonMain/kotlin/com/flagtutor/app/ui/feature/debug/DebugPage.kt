package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable

@Composable
fun DebugPage(
    onNavigateBack: () -> Unit,
    onNavigateToCountriesOverview: () -> Unit,
    onNavigateToFlagAttempts: () -> Unit,
    onNavigateToCrashes: () -> Unit,
) {
    DebugPageContent(
        onCountriesOverviewClick = onNavigateToCountriesOverview,
        onFlagAttemptsClick = onNavigateToFlagAttempts,
        onCrashesClick = onNavigateToCrashes,
        onBackClick = onNavigateBack,
    )
}
