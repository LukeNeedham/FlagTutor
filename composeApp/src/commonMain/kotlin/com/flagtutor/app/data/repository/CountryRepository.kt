package com.flagtutor.app.data.repository

import com.flagtutor.app.data.local.LoadOnce
import com.flagtutor.app.domain.model.Country
import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Reads the countries from `files/country_data.json` (see [Country]). */
class CountryRepository {

    private val countries = LoadOnce {
        val bytes = readFile("files/country_data.json")
        Json.decodeFromString<JsonObject>(bytes.decodeToString())
            .map { (code, element) ->
                val fields = element.jsonObject
                fun text(key: String) = (fields[key] as? JsonPrimitive)?.contentOrNull
                Country(
                    name = text("name") ?: code,
                    alpha2Code = code,
                    wikipediaUrl = text("wikipediaUrl").orEmpty(),
                    flagWikipediaUrl = text("flagWikipediaUrl").orEmpty(),
                    flagImage = text("flagImage"),
                    flagSymbolism = text("symbolism"),
                    flagNote = text("flagNote"),
                )
            }
            .sortedBy { it.name }
    }

    /** The countries that have a flag image, which are the ones the game can show and ask about. */
    suspend fun getCountriesWithFlags(): List<Country> = countries.get().filter { it.hasFlag }

    /** The countries that have an ISO code but no official flag, so they are never shown in the app. */
    suspend fun getCountriesWithoutFlags(): List<Country> = countries.get().filter { !it.hasFlag }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(path: String) = Res.readBytes(path)
}
