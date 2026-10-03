import java.awt.image.BufferedImage

data class ExtractedColor(val container: Int, val content: Int)

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
 * Clusters an image's pixels into its most dominant colors. Runs at build time (see
 * [GenerateFlagColorsTask]) so the app only ships the result, with no per-platform image decoding.
 *
 * Returned colors are opaque `0xRRGGBB` values: the cluster average, plus black or white,
 * whichever is more legible on top of it.
 */
fun extractColorsFromImage(image: BufferedImage, count: Int): List<ExtractedColor> {
    val buckets = mutableMapOf<Int, ColorBucket>()
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val pixel = image.getRGB(x, y)
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
    }

    val totalPixels = image.width * image.height
    return buckets.values
        .sortedByDescending { it.count }
        .filter { it.count >= totalPixels * MIN_PIXEL_FRACTION }
        .take(count)
        .map { bucket ->
            val avgR = (bucket.totalR / bucket.count).toInt()
            val avgG = (bucket.totalG / bucket.count).toInt()
            val avgB = (bucket.totalB / bucket.count).toInt()
            val luminance = (0.299 * avgR + 0.587 * avgG + 0.114 * avgB) / 255.0
            ExtractedColor(
                container = (avgR shl 16) or (avgG shl 8) or avgB,
                content = if (luminance > 0.7) 0x000000 else 0xFFFFFF,
            )
        }
}
