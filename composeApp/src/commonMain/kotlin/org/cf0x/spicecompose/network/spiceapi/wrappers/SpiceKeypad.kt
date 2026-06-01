package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

suspend fun SpiceConnection.keypadsWrite(unit: Int, input: String) {
    val req = SpiceRequest(
        module = "keypads",
        function = "write",
        params = listOf(JsonPrimitive(unit), JsonPrimitive(input))
    )
    request(req)
}

suspend fun SpiceConnection.keypadsSet(unit: Int, buttons: String) {
    val params = mutableListOf<JsonPrimitive>()
    params.add(JsonPrimitive(unit))
    buttons.forEach { params.add(JsonPrimitive(it.toString())) }
    
    val req = SpiceRequest(
        module = "keypads",
        function = "set",
        params = params
    )
    request(req)
}

suspend fun SpiceConnection.keypadsGet(unit: Int): String {
    val req = SpiceRequest(
        module = "keypads",
        function = "get",
        params = listOf(JsonPrimitive(unit))
    )
    val res = request(req)
    return res.data.joinToString("") { it.toString().replace("\"", "") }
}
