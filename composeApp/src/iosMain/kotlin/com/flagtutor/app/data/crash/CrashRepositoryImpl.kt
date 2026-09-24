package com.flagtutor.app.data.crash

import com.flagtutor.app.ui.util.currentTimeMillis
import com.flagtutor.app.ui.util.formatTimestamp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSException
import platform.Foundation.NSSetUncaughtExceptionHandler
import platform.Foundation.NSUserDefaults

private const val CRASH_DEFAULTS_KEY = "flagtutor_crashes"
private const val MAX_STORED_CRASHES = 20

class CrashRepositoryImpl : CrashRepository {

    // NSSetUncaughtExceptionHandler only observes NSExceptions raised across the Objective-C/
    // Swift interop boundary; an unhandled Kotlin exception terminates the process directly and
    // never reaches this handler. This is a best-effort crash log, not a complete one.
    //
    // staticCFunction requires a non-capturing function pointer, so the actual persistence lives
    // in top-level functions rather than instance methods.
    @OptIn(ExperimentalForeignApi::class)
    fun installUncaughtExceptionHandler() {
        NSSetUncaughtExceptionHandler(staticCFunction<NSException?, Unit> { exception ->
            val description = exception?.let { "${it.name}: ${it.reason}\n${it.callStackSymbols}" }
                ?: "Unknown exception"
            persistCrash(description)
        })
    }

    override fun getCrashes(): List<CrashEntry> = loadCrashes().reversed()

    override fun clearCrashes() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(CRASH_DEFAULTS_KEY)
    }
}

private fun persistCrash(stackTrace: String) {
    try {
        val existing = loadCrashes().toMutableList()
        existing.add(CrashEntry(formatTimestamp(currentTimeMillis()), stackTrace))
        val trimmed = if (existing.size > MAX_STORED_CRASHES) existing.takeLast(MAX_STORED_CRASHES) else existing
        NSUserDefaults.standardUserDefaults.setObject(Json.encodeToString(trimmed), forKey = CRASH_DEFAULTS_KEY)
    } catch (_: Exception) {
    }
}

private fun loadCrashes(): List<CrashEntry> {
    val raw = NSUserDefaults.standardUserDefaults.stringForKey(CRASH_DEFAULTS_KEY) ?: return emptyList()
    return try {
        Json.decodeFromString(raw)
    } catch (_: Exception) {
        emptyList()
    }
}
