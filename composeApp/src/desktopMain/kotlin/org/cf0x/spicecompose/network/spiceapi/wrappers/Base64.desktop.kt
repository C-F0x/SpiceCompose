package org.cf0x.spicecompose.network.spiceapi.wrappers

import java.util.Base64

actual fun decodeBase64(base64: String): ByteArray {
    return Base64.getDecoder().decode(base64)
}
