package com.flagtutor.app.data.stats

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class DatabaseBuilderFactory {
    actual fun create(): RoomDatabase.Builder<AppDatabase> {
        val documentDirectory = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        ).first() as String
        val dbFilePath = "$documentDirectory/$DATABASE_FILE_NAME"
        return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
    }
}
