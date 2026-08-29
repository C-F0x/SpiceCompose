package org.cf0x.spicecompose.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.dataWithBytes

/** Saves screenshots under Documents/SpiceCompose. */
@OptIn(ExperimentalForeignApi::class)
actual fun saveImage(bytes: ByteArray, filename: String) {
    try {
        val dir = NSHomeDirectory() + "/Documents/SpiceCompose"
        NSFileManager.defaultManager.createDirectoryAtPath(
            path = dir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        val nsData: NSData = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
        }
        val ok = NSFileManager.defaultManager.createFileAtPath("$dir/$filename", nsData, null)
        if (ok) {
            println("[SpiceCompose] Screenshot saved: $dir/$filename")
        } else {
            println("[SpiceCompose] Failed to save screenshot: $dir/$filename")
        }
    } catch (e: Exception) {
        println("[SpiceCompose] Failed to save screenshot: ${e.message}")
    }
}
