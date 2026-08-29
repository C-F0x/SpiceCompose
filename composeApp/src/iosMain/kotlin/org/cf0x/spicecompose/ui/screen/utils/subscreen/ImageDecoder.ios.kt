package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun decodeToImageBitmap(data: ByteArray): ImageBitmap? {
    return try {
        Image.makeFromEncoded(data).toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
