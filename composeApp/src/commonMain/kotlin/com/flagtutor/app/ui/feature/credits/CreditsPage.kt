package com.flagtutor.app.ui.feature.credits

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun CreditsPage(
    onNavigateBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    CreditsPageContent(
        onLinkClick = { url -> uriHandler.openUri(url) },
        onBackClick = onNavigateBack,
    )
}
