package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable

@Composable
fun DebugPage(
    onNavigateBack: () -> Unit,
    onNavigateToDebugData: () -> Unit,
    onNavigateToFlagAttempts: () -> Unit,
    onNavigateToCrashes: () -> Unit,
) {
    DebugPageContent(
        onDebugDataClick = onNavigateToDebugData,
        onFlagAttemptsClick = onNavigateToFlagAttempts,
        onCrashesClick = onNavigateToCrashes,
        onBackClick = onNavigateBack,
    )
}
