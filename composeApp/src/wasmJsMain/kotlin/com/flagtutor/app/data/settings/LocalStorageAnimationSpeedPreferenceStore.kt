package com.flagtutor.app.data.settings

import kotlinx.browser.localStorage

class LocalStorageAnimationSpeedPreferenceStore : AnimationSpeedPreferenceStore {

    override fun load(): String? = localStorage.getItem(KEY)

    override fun save(value: String) {
        localStorage.setItem(KEY, value)
    }

    private companion object {
        const val KEY = "flagtutor_animation_speed"
    }
}
