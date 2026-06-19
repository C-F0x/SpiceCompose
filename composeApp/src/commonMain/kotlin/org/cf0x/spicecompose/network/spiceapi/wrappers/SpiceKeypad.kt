package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

suspend fun SpiceClient.keypadsWrite(unit: Int, input: String) {
    request(
        "keypads",
        "write",
        params = listOf(JsonPrimitive(unit), JsonPrimitive(input))
    )
}

suspend fun SpiceClient.keypadsSet(unit: Int, buttons: String) {
    val params = mutableListOf<JsonPrimitive>()
    params.add(JsonPrimitive(unit))
    buttons.forEach { params.add(JsonPrimitive(it.toString())) }
    
    request(
        "keypads",
        "set",
        params = params
    )
}

suspend fun SpiceClient.keypadsGet(unit: Int): String {
    val res = request(
        "keypads",
        "get",
        params = listOf(JsonPrimitive(unit))
    )
    return res.jsonObject["data"]?.jsonArray?.joinToString("") { it.toString().replace("\"", "") } ?: ""
}
