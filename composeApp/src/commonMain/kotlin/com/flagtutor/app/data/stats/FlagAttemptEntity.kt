package com.flagtutor.app.data.stats

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flag_attempts")
data class FlagAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val countryId: String,
    val numberOfAttempts: Int,
    val timestamp: Long,
)
