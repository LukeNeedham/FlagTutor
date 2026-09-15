package com.flagtutor.app.data.crash

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashRepositoryImpl(private val context: Context) : CrashRepository {

    private val file: File get() = File(context.filesDir, "crashes.json")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun installUncaughtExceptionHandler() {
        val original = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            persistCrash(throwable)
            original?.uncaughtException(thread, throwable)
        }
    }

    private fun persistCrash(throwable: Throwable) {
        try {
            val existing = load().toMutableList()
            existing.add(CrashEntry(dateFormat.format(Date()), throwable.stackTraceToString()))
            val trimmed = if (existing.size > MAX_STORED) existing.takeLast(MAX_STORED) else existing
            file.writeText(Json.encodeToString(trimmed))
        } catch (_: Exception) {}
    }

    override fun getCrashes(): List<CrashEntry> = load().reversed()

    override fun clearCrashes() {
        file.delete()
    }

    private fun load(): List<CrashEntry> = try {
        if (file.exists()) Json.decodeFromString(file.readText()) else emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    companion object {
        private const val MAX_STORED = 20
    }
}
