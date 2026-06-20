package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import org.cf0x.spicecompose.network.SpiceClient

suspend fun SpiceClient.controlRaise(signal: String) {
    request("control", "raise", listOf(JsonPrimitive(signal)))
}

suspend fun SpiceClient.controlExit(code: Int) {
    request("control", "exit", listOf(JsonPrimitive(code)))
}

suspend fun SpiceClient.controlRestart() {
    request("control", "restart")
}

suspend fun SpiceClient.controlShutdown() {
    request("control", "shutdown")
}

suspend fun SpiceClient.controlReboot() {
    request("control", "reboot")
}
