package com.flagtutor.app.ui.util

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.graphics.Color
import coil3.BitmapImage
import coil3.Image

private const val QUANTIZE_SHIFT = 5
private const val BUCKET_SIZE = 1 shl QUANTIZE_SHIFT

private class ColorBucket {
    var totalR = 0L
    var totalG = 0L
    var totalB = 0L
    var count = 0
}

private const val MIN_PIXEL_FRACTION = 0.02

actual fun extractColorsFromImage(image: Image, count: Int): List<ExtractedColor> {
    val bitmap = (image as? BitmapImage)?.bitmap ?: return emptyList()
    val softwareBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return emptyList()
    } else {
        bitmap
    }
    try {
        val width = softwareBitmap.width
        val height = softwareBitmap.height
        val pixels = IntArray(width * height)
        softwareBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val buckets = mutableMapOf<Int, ColorBucket>()
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val key = (r / BUCKET_SIZE shl 16) or (g / BUCKET_SIZE shl 8) or (b / BUCKET_SIZE)
            val bucket = buckets.getOrPut(key) { ColorBucket() }
            bucket.totalR += r
            bucket.totalG += g
            bucket.totalB += b
            bucket.count++
        }

        val totalPixels = pixels.size
        val sorted = buckets.values.sortedByDescending { it.count }
        return sorted
            .filter { it.count >= totalPixels * MIN_PIXEL_FRACTION }
            .take(count)
            .map { bucket ->
                val avgR = (bucket.totalR / bucket.count).toInt()
                val avgG = (bucket.totalG / bucket.count).toInt()
                val avgB = (bucket.totalB / bucket.count).toInt()
                val containerColor = Color(avgR, avgG, avgB)
                val luminance = (0.299 * avgR + 0.587 * avgG + 0.114 * avgB) / 255.0
                ExtractedColor(
                    containerColor = containerColor,
                    contentColor = if (luminance > 0.7) Color.Black else Color.White,
                )
            }
    } finally {
        if (softwareBitmap !== bitmap) {
            softwareBitmap.recycle()
        }
    }
}
