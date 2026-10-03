package com.flagtutor.app.domain.model

/** How fast UI animations play; [multiplier] above 1 is faster, below 1 is slower. */
enum class AnimationSpeed(val multiplier: Float, val label: String) {
    X2(2f, "2x"),
    X1(1f, "1x"),
    X0_75(0.75f, "0.75x"),
    X0_5(0.5f, "0.5x"),
    X0_25(0.25f, "0.25x"),
}
