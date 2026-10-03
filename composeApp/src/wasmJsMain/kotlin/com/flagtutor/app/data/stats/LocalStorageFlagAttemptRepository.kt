package com.flagtutor.app.data.stats

import com.flagtutor.app.domain.model.FlagAttempt
import com.flagtutor.app.ui.util.currentTimeMillis
import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private const val STORAGE_KEY = "flagtutor.flag_attempts"

// Room has no web support, so attempts are kept as JSON in the browser's localStorage.
class LocalStorageFlagAttemptRepository : FlagAttemptRepository {

    private val serializer = ListSerializer(FlagAttempt.serializer())

    override suspend fun recordAttempt(alpha2Code: String, guessCount: Int) {
        val updated = getAttempts() + FlagAttempt(
            alpha2Code = alpha2Code,
            guessCount = guessCount,
            timestamp = currentTimeMillis(),
        )
        localStorage.setItem(STORAGE_KEY, Json.encodeToString(serializer, updated))
    }

    override suspend fun getAttempts(): List<FlagAttempt> {
        val raw = localStorage.getItem(STORAGE_KEY) ?: return emptyList()
        return try {
            Json.decodeFromString(serializer, raw)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
