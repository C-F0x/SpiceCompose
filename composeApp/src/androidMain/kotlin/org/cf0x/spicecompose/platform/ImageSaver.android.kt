package org.cf0x.spicecompose.platform

import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

actual fun saveImage(bytes: ByteArray, filename: String) {
    try {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, filename)
        FileOutputStream(file).use { it.write(bytes) }
        // Toast requires context — simplified here; Android app handles this
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
