package com.flagtutor.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Records one occurrence of a flag being shown to the user, and how it was answered.
 *
 * @property alpha2Code ISO 3166-1 alpha-2 country code, lowercase, of the flag shown.
 * @property guessCount Number of guesses made before answering correctly (1 = correct first try).
 * @property timestamp Unix epoch milliseconds when the flag was answered correctly.
 */
@Serializable
data class FlagAttempt(
    val alpha2Code: String,
    val guessCount: Int,
    val timestamp: Long,
)
