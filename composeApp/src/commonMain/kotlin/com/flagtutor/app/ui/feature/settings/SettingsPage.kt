package com.flagtutor.app.ui.feature.settings

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsPage(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    SettingsPageContent(
        themeMode = viewModel.themeMode,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onBackClick = onNavigateBack,
    )
}
