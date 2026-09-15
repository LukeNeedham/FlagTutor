package com.flagtutor.app.ui.feature.crashes

import androidx.compose.runtime.Composable
import com.flagtutor.app.data.crash.CrashEntry

@Composable
fun CrashDetailPage(
    crash: CrashEntry,
    onNavigateBack: () -> Unit,
) {
    CrashDetailPageContent(
        crash = crash,
        onBackClick = onNavigateBack,
    )
}
