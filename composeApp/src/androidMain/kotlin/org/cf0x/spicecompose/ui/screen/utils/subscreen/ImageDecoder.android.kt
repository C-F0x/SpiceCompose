package org.cf0x.spicecompose.ui.screen.utils.subscreen

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeToImageBitmap(data: ByteArray): ImageBitmap? {
    return try {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
