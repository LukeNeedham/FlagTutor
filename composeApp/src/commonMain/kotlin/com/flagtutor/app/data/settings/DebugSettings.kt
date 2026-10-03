package com.flagtutor.app.data.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.flagtutor.app.domain.model.AnimationSpeed

/** Debug-only settings. Held in memory, so they reset to defaults when the app restarts. */
class DebugSettings {
    var animationSpeed by mutableStateOf(AnimationSpeed.X1)
}
