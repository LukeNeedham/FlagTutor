package com.flagtutor.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val CODES_URL = "https://flagcdn.com/en/codes.json"

class CountryApi(private val httpClient: HttpClient) {

    /** Returns a map of ISO 3166-1 alpha-2 code (lowercase) to English country name. */
    suspend fun getCountryCodes(): Map<String, String> {
        return httpClient.get(CODES_URL).body()
    }
}
