package org.cf0x.spicecompose.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object SpiceNative {
    init {
        System.loadLibrary("spice_backend")
    }

    actual suspend fun connect(host: String, port: Int, password: String): Boolean =
        withContext(Dispatchers.IO) { nativeConnect(host, port, password) }

    actual suspend fun request(module: String, function: String, paramsJson: String): String =
        withContext(Dispatchers.IO) { nativeRequest(module, function, paramsJson) }

    actual suspend fun disconnect() =
        withContext(Dispatchers.IO) { nativeDisconnect() }

    // JNI native functions — blocking, called from IO dispatcher.
    private external fun nativeConnect(host: String, port: Int, password: String): Boolean
    private external fun nativeRequest(module: String, function: String, paramsJson: String): String
    private external fun nativeDisconnect()
}
