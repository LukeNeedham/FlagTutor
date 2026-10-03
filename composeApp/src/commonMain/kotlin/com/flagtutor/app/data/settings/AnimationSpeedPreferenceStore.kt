package com.flagtutor.app.data.settings

/** Platform-specific persistence for the raw animation speed name. */
interface AnimationSpeedPreferenceStore {
    fun load(): String?
    fun save(value: String)
}
