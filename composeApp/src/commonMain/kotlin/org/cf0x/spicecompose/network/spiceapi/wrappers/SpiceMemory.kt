package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

suspend fun SpiceConnection.memoryWrite(dllName: String, data: String, offset: Int) {
    val req = SpiceRequest(
        module = "memory",
        function = "write",
        params = listOf(JsonPrimitive(dllName), JsonPrimitive(data), JsonPrimitive(offset))
    )
    request(req)
}

suspend fun SpiceConnection.memoryRead(dllName: String, offset: Int, size: Int): String {
    val req = SpiceRequest(
        module = "memory",
        function = "read",
        params = listOf(JsonPrimitive(dllName), JsonPrimitive(offset), JsonPrimitive(size))
    )
    val res = request(req)
    return res.data.getOrNull(0)?.jsonPrimitive?.content ?: ""
}

suspend fun SpiceConnection.memorySignature(
    dllName: String,
    signature: String,
    replacement: String,
    offset: Int,
    usage: Int
): Int {
    val req = SpiceRequest(
        module = "memory",
        function = "signature",
        params = listOf(
            JsonPrimitive(dllName),
            JsonPrimitive(signature),
            JsonPrimitive(replacement),
            JsonPrimitive(offset),
            JsonPrimitive(usage)
        )
    )
    val res = request(req)
    return res.data.getOrNull(0)?.jsonPrimitive?.int ?: 0
}
