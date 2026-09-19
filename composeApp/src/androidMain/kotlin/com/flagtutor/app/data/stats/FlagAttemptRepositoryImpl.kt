package com.flagtutor.app.data.stats

import android.content.Context
import com.flagtutor.app.domain.model.FlagAttempt
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class FlagAttemptRepositoryImpl(private val context: Context) : FlagAttemptRepository {

    private val file: File get() = File(context.filesDir, "flag_attempts.json")

    override fun recordAttempt(alpha2Code: String, guessCount: Int) {
        try {
            val existing = load().toMutableList()
            existing.add(FlagAttempt(alpha2Code, guessCount, System.currentTimeMillis()))
            file.writeText(Json.encodeToString(existing))
        } catch (_: Exception) {}
    }

    override fun getAttempts(): List<FlagAttempt> = load()

    private fun load(): List<FlagAttempt> = try {
        if (file.exists()) Json.decodeFromString(file.readText()) else emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}
