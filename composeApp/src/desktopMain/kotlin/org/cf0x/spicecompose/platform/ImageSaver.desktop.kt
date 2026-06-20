package org.cf0x.spicecompose.platform

import java.io.File

actual fun saveImage(bytes: ByteArray, filename: String) {
    try {
        val dir = File(System.getProperty("user.home"), "SpiceCompose")
        dir.mkdirs()
        val file = File(dir, filename)
        file.writeBytes(bytes)
        println("[SpiceCompose] Screenshot saved: ${file.absolutePath}")
    } catch (e: Exception) {
        println("[SpiceCompose] Failed to save screenshot: ${e.message}")
    }
}
