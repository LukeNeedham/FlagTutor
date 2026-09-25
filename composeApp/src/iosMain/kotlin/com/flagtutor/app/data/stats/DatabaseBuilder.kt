package com.flagtutor.app.data.stats

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class DatabaseBuilderFactory {
    actual fun create(): RoomDatabase.Builder<FlagTutorDatabase> {
        val documentDirectory = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        ).first() as String
        val dbFilePath = "$documentDirectory/$DATABASE_FILE_NAME"
        return Room.databaseBuilder<FlagTutorDatabase>(name = dbFilePath)
    }
}
