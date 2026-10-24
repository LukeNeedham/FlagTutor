package com.flagtutor.app.ui.util

/**
 * The animation durations the app may use, in milliseconds, before the debug animation speed is applied.
 *
 * Every animation spec (`tween`, `delay`, ...) must take its duration from here, never from a raw
 * number, and pass it through the shared library's animation speed so the setting applies to all of them:
 *
 * ```
 * val speed = LocalAnimationSpeed.current
 * tween(speed.scale(AnimationDurations.MEDIUM))
 * ```
 *
 * To add a new duration, add a constant here.
 */
object AnimationDurations {

    /** Quick fades. */
    const val SHORT = 120

    /** Slides between flags, and the Next button leaving. */
    const val MEDIUM = 300

    /** Panels and the Next button entering, and the ripple that erases a wrong option. */
    const val LONG = 400

    /** The colour flood that covers the option buttons when the answer is correct. */
    const val FLOOD = 600
}
