package com.flagtutor.app.ui.util

import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.Image

actual fun extractColorsFromImage(image: Image, count: Int): List<ExtractedColor> {
    val bitmap = (image as? BitmapImage)?.bitmap ?: return emptyList()
    val palette = Palette.from(bitmap).maximumColorCount(count * 2).generate()
    return palette.swatches
        .sortedByDescending { it.population }
        .take(count)
        .map { swatch ->
            ExtractedColor(
                containerColor = Color(swatch.rgb).copy(alpha = 1f),
                contentColor = Color(swatch.titleTextColor).copy(alpha = 1f),
            )
        }
}
