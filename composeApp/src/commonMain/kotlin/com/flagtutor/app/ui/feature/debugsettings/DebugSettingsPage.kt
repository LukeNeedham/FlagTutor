package com.flagtutor.app.ui.feature.debugsettings

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugSettingsPage(
    onNavigateBack: () -> Unit,
    viewModel: DebugSettingsViewModel = koinViewModel(),
) {
    DebugSettingsPageContent(
        animationSpeed = viewModel.animationSpeed,
        onAnimationSpeedSelected = viewModel::onAnimationSpeedSelected,
        onBackClick = onNavigateBack,
    )
}
