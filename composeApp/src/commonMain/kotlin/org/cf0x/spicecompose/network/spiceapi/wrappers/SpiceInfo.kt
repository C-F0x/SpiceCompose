package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

suspend fun SpiceConnection.infoAVS(): Map<String, String> {
    val req = SpiceRequest(module = "info", function = "avs")
    val res = request(req)
    return res.data.getOrNull(0)?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()
}

suspend fun SpiceConnection.infoLauncher(): Map<String, JsonElement> {
    val req = SpiceRequest(module = "info", function = "launcher")
    val res = request(req)
    return res.data.getOrNull(0)?.jsonObject ?: emptyMap()
}

suspend fun SpiceConnection.infoMemory(): Map<String, Long> {
    val req = SpiceRequest(module = "info", function = "memory")
    val res = request(req)
    return res.data.getOrNull(0)?.jsonObject?.mapValues { it.value.jsonPrimitive.long } ?: emptyMap()
}
