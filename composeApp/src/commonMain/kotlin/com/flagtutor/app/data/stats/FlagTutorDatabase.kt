package com.flagtutor.app.data.stats

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [FlagAttemptEntity::class], version = 1, exportSchema = true)
@ConstructedBy(FlagTutorDatabaseConstructor::class)
abstract class FlagTutorDatabase : RoomDatabase() {
    abstract fun flagAttemptDao(): FlagAttemptDao
}

// Room's KSP processor generates the `actual` for this per target, wiring up the generated
// database implementation without reflection (required on Kotlin/Native, where Room can't use
// Class.forName the way it does on Android/JVM).
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object FlagTutorDatabaseConstructor : RoomDatabaseConstructor<FlagTutorDatabase> {
    override fun initialize(): FlagTutorDatabase
}
