package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

suspend fun SpiceClient.memoryWrite(dllName: String, data: String, offset: Int) {
    request(
        "memory",
        "write",
        params = listOf(JsonPrimitive(dllName), JsonPrimitive(data), JsonPrimitive(offset))
    )
}

suspend fun SpiceClient.memoryRead(dllName: String, offset: Int, size: Int): String {
    val res = request(
        "memory",
        "read",
        params = listOf(JsonPrimitive(dllName), JsonPrimitive(offset), JsonPrimitive(size))
    )
    return res.jsonObject["data"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content ?: ""
}

suspend fun SpiceClient.memorySignature(
    dllName: String,
    signature: String,
    replacement: String,
    offset: Int,
    usage: Int
): Int {
    val res = request(
        "memory",
        "signature",
        params = listOf(
            JsonPrimitive(dllName),
            JsonPrimitive(signature),
            JsonPrimitive(replacement),
            JsonPrimitive(offset),
            JsonPrimitive(usage)
        )
    )
    return res.jsonObject["data"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.int ?: 0
}
