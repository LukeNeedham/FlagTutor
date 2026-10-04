package com.flagtutor.app.data.repository

import com.flagtutor.app.data.local.FlagDescriptionDataSource
import com.flagtutor.app.data.local.LoadOnce
import com.flagtutor.app.data.local.WikipediaLinkDataSource
import com.flagtutor.app.domain.model.Country
import flagtutor.composeapp.generated.resources.Res
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val EXCLUDED_CODES = setOf("eu", "un")

class CountryRepository(
    private val wikipediaLinkDataSource: WikipediaLinkDataSource,
    private val flagDescriptionDataSource: FlagDescriptionDataSource,
) {

    private val countries = LoadOnce {
        // Separate fetches (on web, network requests), so run them side by side.
        val (bytes, wikiLinks, descriptions) = coroutineScope {
            val countriesFile = async { readFile("files/countries.json") }
            val links = async { wikipediaLinkDataSource.getLinks() }
            val flagDescriptions = async { flagDescriptionDataSource.getDescriptions() }
            Triple(countriesFile.await(), links.await(), flagDescriptions.await())
        }
        val codes = Json.decodeFromString<JsonObject>(bytes.decodeToString())
        codes.entries
            .filter { it.key.length == 2 && it.key !in EXCLUDED_CODES }
            .map { (code, nameElement) ->
                Country(
                    name = nameElement.jsonPrimitive.content,
                    alpha2Code = code,
                    wikipediaUrl = wikiLinks[code] ?: "",
                    flagDescription = descriptions[code] ?: "",
                )
            }
            .sortedBy { it.name }
    }

    suspend fun getCountries(): List<Country> = countries.get()

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(path: String) = Res.readBytes(path)
}
