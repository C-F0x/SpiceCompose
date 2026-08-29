package org.cf0x.spicecompose.network.spiceapi.wrappers

/** Pure Kotlin Base64 decoder using the standard alphabet. */
actual fun decodeBase64(base64: String): ByteArray {
    val result = ArrayList<Byte>(base64.length * 3 / 4 + 3)
    var buffer = 0
    var bits = 0
    for (c in base64) {
        val value = when (c) {
            in 'A'..'Z' -> (c - 'A')
            in 'a'..'z' -> (c - 'a') + 26
            in '0'..'9' -> (c - '0') + 52
            '+' -> 62
            '/' -> 63
            '=' -> -1
            else -> -2 // Skip whitespace.
        }
        if (value == -2) continue
        if (value == -1) break // Stop at padding.
        buffer = (buffer shl 6) or value
        bits += 6
        if (bits >= 8) {
            bits -= 8
            result.add(((buffer shr bits) and 0xFF).toByte())
        }
    }
    return result.toByteArray()
}
