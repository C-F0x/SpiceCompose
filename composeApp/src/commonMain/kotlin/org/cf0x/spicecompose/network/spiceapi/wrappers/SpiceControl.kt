package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
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

/**
 * session_refresh() — 生成新的随机 API 密码并返回
 * 服务器返回 `data: ["<new_password>"]`
 */
suspend fun SpiceClient.controlSessionRefresh(): String {
    val res = request("control", "session_refresh")
    val data = res.jsonObject["data"]?.jsonArray ?: return ""
    return data.firstOrNull()?.jsonPrimitive?.content ?: ""
}
