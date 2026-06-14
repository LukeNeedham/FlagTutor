package com.flagtutor.app.data.repository

import com.flagtutor.app.data.remote.CountryApi
import com.flagtutor.app.domain.model.Country

class CountryRepository(private val countryApi: CountryApi) {

    private var cachedCountries: List<Country>? = null

    suspend fun getCountries(): List<Country> {
        return cachedCountries ?: countryApi.getAllCountries()
            .filter { it.cca2.length == 2 }
            .map {
                Country(
                    name = it.name.common,
                    alpha2Code = it.cca2.lowercase(),
                    flagUrl = it.flags.png,
                )
            }
            .sortedBy { it.name }
            .also { cachedCountries = it }
    }
}
