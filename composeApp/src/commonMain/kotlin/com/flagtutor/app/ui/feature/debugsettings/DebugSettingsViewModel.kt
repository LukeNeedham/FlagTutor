package com.flagtutor.app.ui.feature.debugsettings

import androidx.lifecycle.ViewModel
import com.flagtutor.app.data.settings.DebugSettings
import com.flagtutor.app.domain.model.AnimationSpeed

class DebugSettingsViewModel(private val settings: DebugSettings) : ViewModel() {

    val animationSpeed: AnimationSpeed get() = settings.animationSpeed

    fun onAnimationSpeedSelected(speed: AnimationSpeed) {
        settings.selectAnimationSpeed(speed)
    }
}
