package com.flagtutor.app.data.repository

import com.flagtutor.app.data.remote.CountryApi
import com.flagtutor.app.domain.model.Country

/** flagcdn.com codes also include non-country entries (e.g. the EU and UN) which aren't playable. */
private val EXCLUDED_CODES = setOf("eu", "un")
private const val FLAG_BASE_URL = "https://flagcdn.com/w320"

class CountryRepository(private val countryApi: CountryApi) {

    private var cachedCountries: List<Country>? = null

    suspend fun getCountries(): List<Country> {
        return cachedCountries ?: countryApi.getCountryCodes()
            .filterKeys { it.length == 2 && it !in EXCLUDED_CODES }
            .map { (code, name) ->
                Country(
                    name = name,
                    alpha2Code = code,
                    flagUrl = "$FLAG_BASE_URL/$code.png",
                )
            }
            .sortedBy { it.name }
            .also { cachedCountries = it }
    }
}
