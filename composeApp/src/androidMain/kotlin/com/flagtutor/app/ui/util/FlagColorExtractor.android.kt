package com.flagtutor.app.ui.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build

actual fun extractColorsFromImage(imageBytes: ByteArray, count: Int): List<ExtractedColor> {
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return emptyList()
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
        return extractColorsFromPixels(pixels, count)
    } finally {
        if (softwareBitmap !== bitmap) {
            softwareBitmap.recycle()
        }
    }
}
