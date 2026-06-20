package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

suspend fun SpiceClient.coinGet(): Int {
    val res = request("coin", "get")
    return res.jsonObject["data"]?.jsonArray?.getOrNull(0).toString().toInt()
}

suspend fun SpiceClient.coinSet(amount: Int) {
    request(
        "coin",
        "set",
        listOf(JsonPrimitive(amount))
    )
}

suspend fun SpiceClient.coinInsert(amount: Int = 1) {
    val params = if (amount != 1) listOf(JsonPrimitive(amount)) else emptyList()
    request("coin", "insert", params)
}

suspend fun SpiceClient.coinBlockerGet(): Boolean {
    val res = request("coin", "blocker_get")
    return res.jsonObject["data"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
}
