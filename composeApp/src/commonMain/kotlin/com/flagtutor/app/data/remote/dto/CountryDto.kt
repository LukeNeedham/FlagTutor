package com.flagtutor.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    val name: NameDto,
    val cca2: String,
    val flags: FlagsDto,
) {
    @Serializable
    data class NameDto(val common: String)

    @Serializable
    data class FlagsDto(val png: String)
}
