package com.flagtutor.app.ui.feature.crashes

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CrashesPage(
    onNavigateBack: () -> Unit,
    viewModel: CrashesViewModel = koinViewModel(),
) {
    CrashesPageContent(
        crashes = viewModel.crashes,
        onClearCrashes = viewModel::clearCrashes,
        onBackClick = onNavigateBack,
    )
}
