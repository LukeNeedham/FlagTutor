package com.flagtutor.app.data.local

import com.flagtutor.app.domain.model.Country
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val KEY_COUNTRIES = "countries"

/** Persists the country list on disk so the app can be played offline. */
class CountryLocalDataSource(private val settings: Settings) {

    fun getCountries(): List<Country>? {
        val json = settings.getStringOrNull(KEY_COUNTRIES) ?: return null
        return runCatching { Json.decodeFromString<List<Country>>(json) }.getOrNull()
    }

    fun saveCountries(countries: List<Country>) {
        settings.putString(KEY_COUNTRIES, Json.encodeToString(countries))
    }
}
