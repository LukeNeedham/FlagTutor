package com.flagtutor.app.data.stats

import com.flagtutor.app.domain.model.FlagAttempt

interface FlagAttemptRepository {
    suspend fun recordAttempt(alpha2Code: String, guessCount: Int)
    suspend fun getAttempts(): List<FlagAttempt>
}
