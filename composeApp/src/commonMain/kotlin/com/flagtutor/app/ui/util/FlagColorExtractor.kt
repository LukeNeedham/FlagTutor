package com.flagtutor.app.ui.util

import androidx.compose.ui.graphics.Color

data class ExtractedColor(
    val containerColor: Color,
    val contentColor: Color,
)

expect fun extractColorsFromImage(imageBytes: ByteArray, count: Int): List<ExtractedColor>
