package com.flagtutor.app.data.stats

import com.flagtutor.app.domain.model.FlagAttempt
import com.flagtutor.app.ui.util.currentTimeMillis

class FlagAttemptRepositoryImpl(private val dao: FlagAttemptDao) : FlagAttemptRepository {

    override suspend fun recordAttempt(alpha2Code: String, guessCount: Int) {
        dao.insert(
            FlagAttemptEntity(
                countryId = alpha2Code,
                numberOfAttempts = guessCount,
                timestamp = currentTimeMillis(),
            )
        )
    }

    override suspend fun getAttempts(): List<FlagAttempt> = dao.getAll().map {
        FlagAttempt(
            alpha2Code = it.countryId,
            guessCount = it.numberOfAttempts,
            timestamp = it.timestamp,
        )
    }
}
