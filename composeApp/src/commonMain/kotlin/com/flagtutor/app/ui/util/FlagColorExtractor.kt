package com.flagtutor.app.ui.util

import androidx.compose.ui.graphics.Color

data class ExtractedColor(
    val containerColor: Color,
    val contentColor: Color,
)

expect fun extractColorsFromImage(imageBytes: ByteArray, count: Int): List<ExtractedColor>

private const val QUANTIZE_SHIFT = 5
private const val BUCKET_SIZE = 1 shl QUANTIZE_SHIFT
private const val MIN_PIXEL_FRACTION = 0.02

private class ColorBucket {
    var totalR = 0L
    var totalG = 0L
    var totalB = 0L
    var count = 0
}

/**
 * Clusters ARGB pixels (packed as `0xAARRGGBB`, matching Android's `Bitmap.getPixels` layout)
 * into the most dominant colors, shared by every platform's [extractColorsFromImage].
 */
internal fun extractColorsFromPixels(pixels: IntArray, count: Int): List<ExtractedColor> {
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
}
