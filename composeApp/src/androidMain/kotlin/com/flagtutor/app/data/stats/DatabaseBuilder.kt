package com.flagtutor.app.data.stats

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseBuilderFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<FlagTutorDatabase> {
        val dbFile = context.getDatabasePath(DATABASE_FILE_NAME)
        return Room.databaseBuilder(context, FlagTutorDatabase::class.java, dbFile.absolutePath)
    }
}
