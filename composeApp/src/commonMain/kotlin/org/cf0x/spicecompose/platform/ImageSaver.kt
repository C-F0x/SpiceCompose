package org.cf0x.spicecompose.platform

/**
 * Save image bytes to a platform-appropriate location.
 * Android: saves to Downloads and shows toast.
 * Desktop: saves to working directory.
 * Web: triggers browser download.
 */
expect fun saveImage(bytes: ByteArray, filename: String)
