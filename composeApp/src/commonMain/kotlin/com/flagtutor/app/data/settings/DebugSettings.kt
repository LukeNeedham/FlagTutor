package com.flagtutor.app.data.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.flagtutor.app.domain.model.AnimationSpeed

/** Debug-only settings, persisted through [animationSpeedStore]. */
class DebugSettings(private val animationSpeedStore: AnimationSpeedPreferenceStore) {

    var animationSpeed by mutableStateOf(
        AnimationSpeed.entries.firstOrNull { it.name == animationSpeedStore.load() } ?: AnimationSpeed.X1,
    )
        private set

    fun selectAnimationSpeed(speed: AnimationSpeed) {
        animationSpeed = speed
        animationSpeedStore.save(speed.name)
    }
}
