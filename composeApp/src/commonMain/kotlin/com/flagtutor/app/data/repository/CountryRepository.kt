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
                    flagSharedWith = text("flagSharedWith"),
                )
            }
            .sortedBy { it.name }
    }

    /**
     * The countries the app shows and asks about: those with a flag, except territories that use their parent's flag,
     * so each flag appears once, as its parent. Everything else is in [getExcludedCountries].
     */
    suspend fun getGameCountries(): List<Country> = countries.get().filter { it.hasFlag && it.flagSharedWith == null }

    /**
     * The countries the app leaves out: those without a flag of their own, and territories that use their parent's flag
     * (which the game skips so that each flag appears once, as its parent).
     */
    suspend fun getExcludedCountries(): List<Country> = countries.get().filter { !it.hasFlag || it.flagSharedWith != null }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(path: String) = Res.readBytes(path)
}
