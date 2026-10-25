package com.flagtutor.app.screenshot

import com.flagtutor.app.ui.theme.FlagTutorTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flagtutor.app.ui.feature.credits.CreditsPageContent
import com.flagtutor.app.ui.theme.AppTheme

/** Rendered by CI to docs/screenshots/<position in order.txt>-CreditsPage.png: the credits. */
@Preview
@Composable
private fun CreditsPageScreenshot() {
    FlagTutorTheme {
        CreditsPageContent(onLinkClick = {}, onBackClick = {})
    }
}
