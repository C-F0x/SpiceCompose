package org.cf0x.spicecompose.network.spiceapi.wrappers

actual fun decodeBase64(base64: String): ByteArray {
    // Pure Kotlin implementation — no JS interop needed.
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val clean = base64.replace("\n", "").replace("\r", "").replace("=", "")
    val result = mutableListOf<Byte>()

    var i = 0
    while (i < clean.length) {
        val b0 = chars.indexOf(clean.getOrElse(i) { 'A' })
        val b1 = chars.indexOf(clean.getOrElse(i + 1) { 'A' })
        val b2 = chars.indexOf(clean.getOrElse(i + 2) { 'A' })
        val b3 = chars.indexOf(clean.getOrElse(i + 3) { 'A' })

        result.add(((b0 shl 2) or (b1 shr 4)).toByte())
        if (i + 2 < clean.length) result.add(((b1 shl 4) or (b2 shr 2)).toByte())
        if (i + 3 < clean.length) result.add(((b2 shl 6) or b3).toByte())
        i += 4
    }
    return result.toByteArray()
}
