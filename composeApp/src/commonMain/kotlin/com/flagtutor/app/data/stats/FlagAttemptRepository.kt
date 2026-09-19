package com.flagtutor.app.data.stats

import com.flagtutor.app.domain.model.FlagAttempt

interface FlagAttemptRepository {
    fun recordAttempt(alpha2Code: String, guessCount: Int)
    fun getAttempts(): List<FlagAttempt>
}
