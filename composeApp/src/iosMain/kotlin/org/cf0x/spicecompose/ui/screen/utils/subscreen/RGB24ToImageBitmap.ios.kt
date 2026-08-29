package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.*

/**
 * Convert raw RGB24 pixel data to a Compose [ImageBitmap].
 *
 * Uses Skiko, matching the desktop implementation.
 */
actual fun rgb24ToImageBitmap(width: Int, height: Int, rgb24: ByteArray): ImageBitmap? {
    return try {
        val bgra = ByteArray(width * height * 4)
        for (i in 0 until width * height) {
            val r = rgb24[i * 3]
            val g = rgb24[i * 3 + 1]
            val b = rgb24[i * 3 + 2]
            bgra[i * 4] = b
            bgra[i * 4 + 1] = g
            bgra[i * 4 + 2] = r
            bgra[i * 4 + 3] = (-1).toByte()
        }
        val info = ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.PREMUL)
        val bitmap = Bitmap()
        bitmap.installPixels(info, bgra, width * 4)
        Image.makeFromBitmap(bitmap).toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
