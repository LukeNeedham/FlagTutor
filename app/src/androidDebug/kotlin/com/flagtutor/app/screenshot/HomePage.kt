package com.flagtutor.app.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flagtutor.app.ui.feature.home.HomePageContent
import com.flagtutor.app.ui.theme.AppTheme

/** Rendered by CI to docs/screenshots/<position in order.txt>-HomePage.png: the home page. */
@Preview
@Composable
private fun HomePageScreenshot() {
    AppTheme {
        HomePageContent(
            title = "Vexed",
            subtitle = "Learn the flags of the world",
            onGuessCountryClick = {},
            onCreditsClick = {},
            onSettingsClick = {},
        )
    }
}
