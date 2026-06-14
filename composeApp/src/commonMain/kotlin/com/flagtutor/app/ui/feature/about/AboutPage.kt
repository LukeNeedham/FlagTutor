package com.flagtutor.app.ui.feature.about

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AboutPage(
    onNavigateBack: () -> Unit,
    viewModel: AboutViewModel = koinViewModel(),
) {
    AboutPageContent(
        message = viewModel.message,
        onBackClick = onNavigateBack,
    )
}
