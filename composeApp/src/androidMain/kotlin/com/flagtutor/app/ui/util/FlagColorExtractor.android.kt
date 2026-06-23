package com.flagtutor.app.ui.util

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.Image

actual fun extractColorsFromImage(image: Image, count: Int): List<ExtractedColor> {
    val bitmap = (image as? BitmapImage)?.bitmap ?: return emptyList()
    val softwareBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return emptyList()
    } else {
        bitmap
    }
    try {
        val palette = Palette.from(softwareBitmap).maximumColorCount(count * 2).generate()
        return palette.swatches
            .sortedByDescending { it.population }
            .take(count)
            .map { swatch ->
                ExtractedColor(
                    containerColor = Color(swatch.rgb).copy(alpha = 1f),
                    contentColor = Color(swatch.titleTextColor).copy(alpha = 1f),
                )
            }
    } finally {
        if (softwareBitmap !== bitmap) {
            softwareBitmap.recycle()
        }
    }
}
