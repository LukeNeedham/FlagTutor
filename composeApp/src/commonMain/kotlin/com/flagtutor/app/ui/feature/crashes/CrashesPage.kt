package com.flagtutor.app.ui.feature.crashes

import androidx.compose.runtime.Composable
import com.flagtutor.app.data.crash.CrashEntry
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CrashesPage(
    onNavigateBack: () -> Unit,
    onNavigateToCrashDetail: (CrashEntry) -> Unit,
    viewModel: CrashesViewModel = koinViewModel(),
) {
    CrashesPageContent(
        crashes = viewModel.crashes,
        onClearCrashes = viewModel::clearCrashes,
        onCrashClick = onNavigateToCrashDetail,
        onBackClick = onNavigateBack,
    )
}
