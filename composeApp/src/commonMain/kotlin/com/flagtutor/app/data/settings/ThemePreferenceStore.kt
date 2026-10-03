package com.flagtutor.app.data.settings

/** Platform-specific persistence for the raw theme mode name. */
interface ThemePreferenceStore {
    fun load(): String?
    fun save(value: String)
}
