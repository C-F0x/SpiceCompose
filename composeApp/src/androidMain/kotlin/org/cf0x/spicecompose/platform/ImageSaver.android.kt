package org.cf0x.spicecompose.platform

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore

actual fun saveImage(bytes: ByteArray, filename: String) {
    val context = ImageSaverContextHolder.context ?: return
    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "image/jpeg")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(bytes)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/** Holder for Android application context, initialized from MainActivity. */
object ImageSaverContextHolder {
    var context: Context? = null
}
