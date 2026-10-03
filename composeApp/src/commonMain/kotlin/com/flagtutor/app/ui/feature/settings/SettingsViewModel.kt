package com.flagtutor.app.ui.feature.settings

import androidx.lifecycle.ViewModel
import com.flagtutor.app.data.settings.DebugSettings
import com.flagtutor.app.data.settings.ThemeRepository
import com.flagtutor.app.domain.model.AnimationSpeed
import com.flagtutor.app.domain.model.ThemeMode

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val debugSettings: DebugSettings,
) : ViewModel() {

    val themeMode: ThemeMode get() = themeRepository.themeMode

    val animationSpeed: AnimationSpeed get() = debugSettings.animationSpeed

    fun onThemeModeSelected(mode: ThemeMode) = themeRepository.selectThemeMode(mode)

    fun onAnimationSpeedSelected(speed: AnimationSpeed) = debugSettings.selectAnimationSpeed(speed)
}
