package com.flagtutor.app.domain.model

import kotlinx.serialization.Serializable

/**
 * One entry of `files/country_data.json`, which is built from Wikipedia by the `generateCountryData` Gradle task.
 *
 * @property alpha2Code ISO 3166-1 alpha-2 country code, lowercase (e.g. "fr" for France).
 * @property name the ISO English short name.
 * @property wikipediaUrl the country's Wikipedia article, empty if none was found.
 * @property flagWikipediaUrl the Wikipedia article dedicated to the country's flag, empty if none was found.
 * @property flagImage path of the flag image under `files/` (always `flags/<alpha2Code>.png`), or null if the
 * country has no official flag of its own, in which case there is nothing to show or ask about.
 * @property flagSymbolism a short text on what the flag symbolises, null if there is no flag.
 * @property flagNote why the country has no flag image, null if it has one.
 */
@Serializable
data class Country(
    val name: String,
    val alpha2Code: String,
    val wikipediaUrl: String,
    val flagWikipediaUrl: String,
    val flagImage: String?,
    val flagSymbolism: String?,
    val flagNote: String?,
) {
    val hasFlag: Boolean get() = flagImage != null
}
