package com.flagtutor.app.ui.util

import androidx.compose.ui.graphics.Color
import coil3.Image

data class ExtractedColor(
    val containerColor: Color,
    val contentColor: Color,
)

expect fun extractColorsFromImage(image: Image, count: Int): List<ExtractedColor>
