package com.flagtutor.app.domain.model

import kotlinx.serialization.Serializable

/**
 * @property alpha2Code ISO 3166-1 alpha-2 country code, lowercase (e.g. "fr" for France).
 */
@Serializable
data class Country(
    val name: String,
    val alpha2Code: String,
    val wikipediaUrl: String,
)
