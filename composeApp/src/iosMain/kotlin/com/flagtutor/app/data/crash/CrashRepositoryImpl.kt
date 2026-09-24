package com.flagtutor.app.data.crash

import com.flagtutor.app.ui.util.currentTimeMillis
import com.flagtutor.app.ui.util.formatTimestamp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSException
import platform.Foundation.NSSetUncaughtExceptionHandler
import platform.Foundation.NSUserDefaults

class CrashRepositoryImpl : CrashRepository {

    private val defaults = NSUserDefaults.standardUserDefaults

    // NSSetUncaughtExceptionHandler only observes NSExceptions raised across the Objective-C/
    // Swift interop boundary; an unhandled Kotlin exception terminates the process directly and
    // never reaches this handler. This is a best-effort crash log, not a complete one.
    fun installUncaughtExceptionHandler() {
        NSSetUncaughtExceptionHandler { exception: NSException? ->
            val description = exception?.let { "${it.name}: ${it.reason}\n${it.callStackSymbols}" }
                ?: "Unknown exception"
            persistCrash(description)
        }
    }

    private fun persistCrash(stackTrace: String) {
        try {
            val existing = load().toMutableList()
            existing.add(CrashEntry(formatTimestamp(currentTimeMillis()), stackTrace))
            val trimmed = if (existing.size > MAX_STORED) existing.takeLast(MAX_STORED) else existing
            defaults.setObject(Json.encodeToString(trimmed), forKey = KEY)
        } catch (_: Exception) {
        }
    }

    override fun getCrashes(): List<CrashEntry> = load().reversed()

    override fun clearCrashes() {
        defaults.removeObjectForKey(KEY)
    }

    private fun load(): List<CrashEntry> {
        val raw = defaults.stringForKey(KEY) ?: return emptyList()
        return try {
            Json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY = "flagtutor_crashes"
        private const val MAX_STORED = 20
    }
}
