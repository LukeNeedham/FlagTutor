package com.flagtutor.app.data.stats

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FlagAttemptEntity::class], version = 1, exportSchema = true)
abstract class FlagTutorDatabase : RoomDatabase() {
    abstract fun flagAttemptDao(): FlagAttemptDao
}
