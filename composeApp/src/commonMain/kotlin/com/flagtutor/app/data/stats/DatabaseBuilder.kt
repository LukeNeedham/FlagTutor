package com.flagtutor.app.data.stats

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

internal const val DATABASE_FILE_NAME = "flagtutor.db"

// Constructed per-platform (needs a Context on Android, nothing extra on iOS), then handed to
// createDatabase() to apply the settings shared across platforms.
expect class DatabaseBuilderFactory {
    fun create(): RoomDatabase.Builder<FlagTutorDatabase>
}

fun createDatabase(builder: RoomDatabase.Builder<FlagTutorDatabase>): FlagTutorDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
