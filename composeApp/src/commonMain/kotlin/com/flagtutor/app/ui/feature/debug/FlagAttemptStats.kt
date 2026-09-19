package com.flagtutor.app.ui.feature.debug

data class FlagAttemptStats(
    val totalAttempts: Int,
    val totalIncorrectAnswers: Int,
    val averageIncorrectPerAttempt: Double,
)
