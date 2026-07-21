package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

suspend fun SpiceClient.infoAVS(): Map<String, String> {
    val res = request("info", "avs")
    return res.jsonObject["data"]?.jsonArray?.getOrNull(0)?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()
}

suspend fun SpiceClient.infoLauncher(): Map<String, String> {
    val res = request("info", "launcher")
    val obj = res.jsonObject["data"]?.jsonArray?.getOrNull(0)?.jsonObject ?: return emptyMap()
    return obj.mapValues { (key, value) ->
        when {
            // args is a JSON array of strings → join them
            key == "args" && value is JsonArray ->
                value.joinToString(" ") { it.jsonPrimitive.content }
            else ->
                value.jsonPrimitive.content
        }
    }
}

suspend fun SpiceClient.infoMemory(): Map<String, Long> {
    val res = request("info", "memory")
    return res.jsonObject["data"]?.jsonArray?.getOrNull(0)?.jsonObject?.mapValues { it.value.jsonPrimitive.long } ?: emptyMap()
}
