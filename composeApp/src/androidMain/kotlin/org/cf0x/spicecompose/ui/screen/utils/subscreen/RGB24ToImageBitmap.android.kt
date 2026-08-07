package org.cf0x.spicecompose.ui.screen.utils.subscreen

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun rgb24ToImageBitmap(width: Int, height: Int, rgb24: ByteArray): ImageBitmap? {
    return try {
        val pixels = IntArray(width * height)
        for (i in 0 until width * height) {
            val r = rgb24[i * 3].toInt() and 0xFF
            val g = rgb24[i * 3 + 1].toInt() and 0xFF
            val b = rgb24[i * 3 + 2].toInt() and 0xFF
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
