package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.Composable

@Composable
fun DebugPage(
    onNavigateBack: () -> Unit,
    onNavigateToDebugData: () -> Unit,
    onNavigateToCrashes: () -> Unit,
) {
    DebugPageContent(
        onDebugDataClick = onNavigateToDebugData,
        onCrashesClick = onNavigateToCrashes,
        onBackClick = onNavigateBack,
    )
}
