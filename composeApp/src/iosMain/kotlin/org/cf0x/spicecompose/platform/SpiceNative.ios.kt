package org.cf0x.spicecompose.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** iOS bridge to the Rust static library through Kotlin/Native cinterop.
 * Returned strings are freed with spice_native_free_string.
 */
actual object SpiceNative {
    actual suspend fun connect(host: String, port: Int, password: String): Boolean =
        withContext(Dispatchers.Default) { nativeConnect(host, port, password) }

    actual suspend fun request(module: String, function: String, paramsJson: String): String =
        withContext(Dispatchers.Default) { nativeRequest(module, function, paramsJson) }

    actual suspend fun touchRequest(module: String, function: String, paramsJson: String): String =
        withContext(Dispatchers.Default) { nativeTouchRequest(module, function, paramsJson) }

    actual suspend fun disconnect() =
        withContext(Dispatchers.Default) { nativeDisconnect() }

    @OptIn(ExperimentalForeignApi::class)
    private fun nativeConnect(host: String, port: Int, password: String): Boolean {
        if (host.isEmpty()) return false
        return memScoped {
            val h = host.cstr.getPointer(memScope)
            val p = password.cstr.getPointer(memScope)
            spice_native_connect(h, port, p)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun nativeRequest(module: String, function: String, paramsJson: String): String {
        if (module.isEmpty()) return """{"error":"empty module"}"""
        return memScoped {
            val m = module.cstr.getPointer(memScope)
            val f = function.cstr.getPointer(memScope)
            val p = paramsJson.cstr.getPointer(memScope)
            val res = spice_native_request(m, f, p) ?: return """{"error":"null result"}"""
            try {
                res.toKString()
            } finally {
                spice_native_free_string(res)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun nativeTouchRequest(module: String, function: String, paramsJson: String): String {
        if (module.isEmpty()) return """{"error":"empty module"}"""
        return memScoped {
            val m = module.cstr.getPointer(memScope)
            val f = function.cstr.getPointer(memScope)
            val p = paramsJson.cstr.getPointer(memScope)
            val res = spice_native_touch_request(m, f, p) ?: return """{"error":"null result"}"""
            try {
                res.toKString()
            } finally {
                spice_native_free_string(res)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun nativeDisconnect() {
        spice_native_disconnect()
    }
}
