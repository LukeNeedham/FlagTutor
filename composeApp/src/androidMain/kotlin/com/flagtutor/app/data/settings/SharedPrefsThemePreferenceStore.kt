package com.flagtutor.app.data.settings

import android.content.Context

class SharedPrefsThemePreferenceStore(context: Context) : ThemePreferenceStore {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override fun load(): String? = prefs.getString(KEY, null)

    override fun save(value: String) {
        prefs.edit().putString(KEY, value).apply()
    }

    private companion object {
        const val KEY = "theme_mode"
    }
}
