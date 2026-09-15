package com.flagtutor.app.data.crash

import kotlinx.serialization.Serializable

@Serializable
data class CrashEntry(
    val timestamp: String,
    val stackTrace: String,
)
