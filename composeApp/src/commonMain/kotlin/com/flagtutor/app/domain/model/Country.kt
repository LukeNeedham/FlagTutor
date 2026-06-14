package com.flagtutor.app.domain.model

/**
 * @property alpha2Code ISO 3166-1 alpha-2 country code, lowercase (e.g. "fr" for France).
 *   Matches the naming convention of the round-flags icon set (`flag_<alpha2Code>`).
 */
data class Country(
    val name: String,
    val alpha2Code: String,
)
