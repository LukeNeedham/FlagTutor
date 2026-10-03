package com.flagtutor.app.ui.util

import androidx.compose.runtime.compositionLocalOf
import kotlin.math.roundToInt

/** The current animation speed multiplier (see [com.flagtutor.app.domain.model.AnimationSpeed]). */
val LocalAnimationSpeed = compositionLocalOf { 1f }

/** This duration in milliseconds, stretched or shrunk for the given animation [speed]. */
fun Int.scaledBy(speed: Float): Int = (this / speed).roundToInt()

fun Long.scaledBy(speed: Float): Long = (this / speed).toLong()
