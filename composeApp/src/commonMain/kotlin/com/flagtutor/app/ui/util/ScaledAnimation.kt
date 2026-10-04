package com.flagtutor.app.ui.util

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import kotlin.math.roundToInt

/**
 * The animation durations the app may use, with the debug animation speed already applied.
 *
 * Every animation spec (`tween`, `delay`, ...) must take its duration from here, never from a raw
 * number, so the animation speed setting applies to all of them. Read it with [LocalScaledAnimation].
 * To add a new duration, add a property here.
 *
 * @param speed the speed multiplier: above 1 is faster, below 1 is slower.
 */
@Immutable
class ScaledAnimation(private val speed: Float) {

    /** Quick fades. */
    val short: Int = scaled(120)

    /** Slides between flags, and the Next button leaving. */
    val medium: Int = scaled(300)

    /** Panels and the Next button entering, and the ripple that erases a wrong option. */
    val long: Int = scaled(400)

    private fun scaled(durationMs: Int): Int = (durationMs / speed).roundToInt()
}

val LocalScaledAnimation = compositionLocalOf { ScaledAnimation(speed = 1f) }
