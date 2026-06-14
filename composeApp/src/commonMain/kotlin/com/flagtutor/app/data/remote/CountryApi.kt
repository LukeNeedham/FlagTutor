package com.flagtutor.app.data.remote

import com.flagtutor.app.data.remote.dto.CountryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private const val BASE_URL = "https://restcountries.com/v3.1"

class CountryApi(private val httpClient: HttpClient) {

    suspend fun getAllCountries(): List<CountryDto> {
        return httpClient.get("$BASE_URL/all") {
            parameter("fields", "name,cca2,flags")
        }.body()
    }
}
