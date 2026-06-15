package com.flagtutor.app.data.repository

import com.flagtutor.app.data.local.CountryLocalDataSource
import com.flagtutor.app.data.remote.CountryApi
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.CancellationException

/** flagcdn.com codes also include non-country entries (e.g. the EU and UN) which aren't playable. */
private val EXCLUDED_CODES = setOf("eu", "un")
private const val FLAG_BASE_URL = "https://flagcdn.com/w320"

data class CountriesResult(val countries: List<Country>, val isFromCache: Boolean)

class CountryRepository(
    private val countryApi: CountryApi,
    private val localDataSource: CountryLocalDataSource,
) {

    /** Returns the cached country list if one exists, otherwise fetches and caches it. */
    suspend fun getCountries(): CountriesResult {
        val cached = localDataSource.getCountries()
        if (cached != null) {
            return CountriesResult(cached, isFromCache = true)
        }
        return CountriesResult(fetchAndPersist(), isFromCache = false)
    }

    /** Fetches the latest country data and updates the cache. Returns null if offline. */
    suspend fun refreshInBackground(): List<Country>? {
        return try {
            fetchAndPersist()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchAndPersist(): List<Country> {
        val countries = countryApi.getCountryCodes()
            .filterKeys { it.length == 2 && it !in EXCLUDED_CODES }
            .map { (code, name) ->
                Country(
                    name = name,
                    alpha2Code = code,
                    flagUrl = "$FLAG_BASE_URL/$code.png",
                )
            }
            .sortedBy { it.name }
        localDataSource.saveCountries(countries)
        return countries
    }
}
