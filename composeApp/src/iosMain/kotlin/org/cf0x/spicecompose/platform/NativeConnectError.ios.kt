package org.cf0x.spicecompose.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
actual fun lastNativeConnectError(): String {
    val res = spice_native_last_error() ?: return ""
    return try {
        res.toKString()
    } finally {
        spice_native_free_string(res)
    }
}
