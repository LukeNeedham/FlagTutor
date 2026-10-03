package com.flagtutor.app.ui.feature.settings

import androidx.lifecycle.ViewModel
import com.flagtutor.app.data.settings.ThemeRepository
import com.flagtutor.app.domain.model.ThemeMode

class SettingsViewModel(private val themeRepository: ThemeRepository) : ViewModel() {

    val themeMode: ThemeMode get() = themeRepository.themeMode

    fun onThemeModeSelected(mode: ThemeMode) = themeRepository.selectThemeMode(mode)
}
