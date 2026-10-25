package com.flagtutor.app.screenshot

import com.flagtutor.app.ui.theme.FlagTutorTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flagtutor.app.domain.model.ThemeMode
import com.flagtutor.app.ui.feature.settings.SettingsPageContent
import com.flagtutor.app.ui.theme.AppTheme

/** Rendered by CI to docs/screenshots/<position in order.txt>-SettingsPage.png: choosing the theme. */
@Preview
@Composable
private fun SettingsPageScreenshot() {
    FlagTutorTheme {
        SettingsPageContent(themeMode = ThemeMode.System, onThemeModeSelected = {}, onBackClick = {})
    }
}
