package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

suspend fun SpiceConnection.coinGet(): Int {
    val req = SpiceRequest(module = "coin", function = "get")
    val res = request(req)
    return res.data[0].toString().toInt()
}

suspend fun SpiceConnection.coinSet(amount: Int) {
    val req = SpiceRequest(
        module = "coin",
        function = "set",
        params = listOf(JsonPrimitive(amount))
    )
    request(req)
}

suspend fun SpiceConnection.coinInsert(amount: Int = 1) {
    val params = if (amount != 1) listOf(JsonPrimitive(amount)) else emptyList()
    val req = SpiceRequest(
        module = "coin",
        function = "insert",
        params = params
    )
    request(req)
}
