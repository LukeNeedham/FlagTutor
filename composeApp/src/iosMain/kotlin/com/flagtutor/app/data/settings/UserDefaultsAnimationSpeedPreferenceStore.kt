package com.flagtutor.app.data.settings

import platform.Foundation.NSUserDefaults

class UserDefaultsAnimationSpeedPreferenceStore : AnimationSpeedPreferenceStore {

    override fun load(): String? = NSUserDefaults.standardUserDefaults.stringForKey(KEY)

    override fun save(value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = KEY)
    }

    private companion object {
        const val KEY = "flagtutor_animation_speed"
    }
}
