package com.flagtutor.app.ui.util

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

actual fun extractColorsFromImage(imageBytes: ByteArray, count: Int): List<ExtractedColor> {
    val image = try {
        Image.makeFromEncoded(imageBytes)
    } catch (_: Exception) {
        return emptyList()
    }

    val imageInfo = ImageInfo(
        colorType = ColorType.RGBA_8888,
        alphaType = ColorAlphaType.UNPREMUL,
        colorSpace = null,
        width = image.width,
        height = image.height,
    )
    val bitmap = Bitmap().apply { allocPixels(imageInfo) }
    if (!image.readPixels(bitmap, 0, 0)) return emptyList()
    val rgba = bitmap.readPixels() ?: return emptyList()

    val pixelCount = image.width * image.height
    val pixels = IntArray(pixelCount)
    for (i in 0 until pixelCount) {
        val offset = i * 4
        val r = rgba[offset].toInt() and 0xFF
        val g = rgba[offset + 1].toInt() and 0xFF
        val b = rgba[offset + 2].toInt() and 0xFF
        val a = rgba[offset + 3].toInt() and 0xFF
        // Repack into Android's 0xAARRGGBB layout so the shared bucketing logic applies as-is.
        pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    return extractColorsFromPixels(pixels, count)
}
