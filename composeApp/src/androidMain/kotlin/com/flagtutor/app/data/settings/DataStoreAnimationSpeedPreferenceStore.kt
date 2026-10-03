package com.flagtutor.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DataStoreAnimationSpeedPreferenceStore(context: Context) : AnimationSpeedPreferenceStore {

    private val dataStore = context.applicationContext.settingsDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Read once at startup so the saved speed applies from the first frame; the file is tiny.
    override fun load(): String? = runBlocking { dataStore.data.first()[KEY] }

    override fun save(value: String) {
        scope.launch { dataStore.edit { it[KEY] = value } }
    }

    private companion object {
        val KEY = stringPreferencesKey("animation_speed")
    }
}
