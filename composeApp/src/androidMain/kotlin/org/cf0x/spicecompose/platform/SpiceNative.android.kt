package org.cf0x.spicecompose.platform

actual object SpiceNative {
    init {
        System.loadLibrary("spice_backend")
    }

    actual external fun connect(host: String, port: Int, password: String): Boolean
    actual external fun request(module: String, function: String, paramsJson: String): String
    actual external fun disconnect()
}
