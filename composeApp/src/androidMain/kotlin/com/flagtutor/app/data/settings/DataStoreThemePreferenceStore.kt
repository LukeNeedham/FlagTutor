package com.flagtutor.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreThemePreferenceStore(context: Context) : ThemePreferenceStore {

    private val dataStore = context.applicationContext.settingsDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Read once at startup so the saved theme is applied on the first frame (no flash of the
    // wrong theme); the file is tiny.
    override fun load(): String? = runBlocking { dataStore.data.first()[KEY] }

    override fun save(value: String) {
        scope.launch { dataStore.edit { it[KEY] = value } }
    }

    private companion object {
        val KEY = stringPreferencesKey("theme_mode")
    }
}
