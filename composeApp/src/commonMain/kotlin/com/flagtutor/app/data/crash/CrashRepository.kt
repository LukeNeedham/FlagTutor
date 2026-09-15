package com.flagtutor.app.data.crash

interface CrashRepository {
    fun getCrashes(): List<CrashEntry>
    fun clearCrashes()
}
