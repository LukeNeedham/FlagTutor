package com.flagtutor.app.data.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FlagAttemptDao {
    @Insert
    suspend fun insert(entity: FlagAttemptEntity)

    @Query("SELECT * FROM flag_attempts ORDER BY timestamp ASC")
    suspend fun getAll(): List<FlagAttemptEntity>
}
