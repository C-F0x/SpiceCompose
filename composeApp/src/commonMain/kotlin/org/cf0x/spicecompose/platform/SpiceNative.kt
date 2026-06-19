package org.cf0x.spicecompose.platform

/**
 * Platform-native bridge to the Rust spice-backend.
 *
 * Android: JNI calls into libspice_backend.so
 * Desktop: HTTP to localhost:9800
 * Web:     fetch() to Rust backend
 */
expect object SpiceNative {
    suspend fun connect(host: String, port: Int, password: String): Boolean
    suspend fun request(module: String, function: String, paramsJson: String): String
    suspend fun disconnect()
}
