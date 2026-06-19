package org.cf0x.spicecompose.platform

/**
 * Platform-native bridge to the Rust spice-backend.
 *
 * Android: JNI calls into libspice_backend.so
 * Desktop: delegates to a local HTTP SpiceClient (or subprocess)
 */
expect object SpiceNative {
    fun connect(host: String, port: Int, password: String): Boolean
    fun request(module: String, function: String, paramsJson: String): String
    fun disconnect()
}
