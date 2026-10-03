package com.flagtutor.app.ui.feature.settings

import androidx.compose.runtime.Composable
import com.flagtutor.app.isDebugBuild
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsPage(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    SettingsPageContent(
        themeMode = viewModel.themeMode,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        showDebugSettings = isDebugBuild,
        animationSpeed = viewModel.animationSpeed,
        onAnimationSpeedSelected = viewModel::onAnimationSpeedSelected,
        onBackClick = onNavigateBack,
    )
}
