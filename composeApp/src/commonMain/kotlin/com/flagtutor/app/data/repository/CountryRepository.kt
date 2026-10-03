package com.flagtutor.app.data.repository

import com.flagtutor.app.data.local.WikipediaLinkDataSource
import com.flagtutor.app.domain.model.Country
import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val EXCLUDED_CODES = setOf("eu", "un")

class CountryRepository(
    private val wikipediaLinkDataSource: WikipediaLinkDataSource,
) {

    private var cached: List<Country>? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun getCountries(): List<Country> {
        cached?.let { return it }
        val bytes = Res.readBytes("files/countries.json")
        val codes = Json.decodeFromString<JsonObject>(bytes.decodeToString())
        val wikiLinks = wikipediaLinkDataSource.getLinks()
        val countries = codes.entries
            .filter { it.key.length == 2 && it.key !in EXCLUDED_CODES }
            .map { (code, nameElement) ->
                Country(
                    name = nameElement.jsonPrimitive.content,
                    alpha2Code = code,
                    wikipediaUrl = wikiLinks[code] ?: "",
                )
            }
            .sortedBy { it.name }
        cached = countries
        return countries
    }
}
