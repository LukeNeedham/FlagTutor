package com.flagtutor.app.data.crash

// Crash logs aren't persisted on web: the browser console already captures uncaught errors.
class CrashRepositoryImpl : CrashRepository {
    override fun getCrashes(): List<CrashEntry> = emptyList()
    override fun clearCrashes() = Unit
}
