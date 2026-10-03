package com.flagtutor.app.data.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.flagtutor.app.domain.model.ThemeMode

class ThemeRepository(private val store: ThemePreferenceStore) {

    var themeMode by mutableStateOf(
        ThemeMode.entries.firstOrNull { it.name == store.load() } ?: ThemeMode.System,
    )
        private set

    fun setThemeMode(mode: ThemeMode) {
        themeMode = mode
        store.save(mode.name)
    }
}
