package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.cf0x.spicecompose.network.SpiceClient

suspend fun SpiceClient.iidxTickerGet(): String {
    val res = request("iidx", "ticker_get")
    return res.jsonObject["data"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content ?: ""
}

suspend fun SpiceClient.iidxTickerSet(text: String) {
    request("iidx", "ticker_set", listOf(JsonPrimitive(text)))
}

suspend fun SpiceClient.iidxTickerReset() {
    request("iidx", "ticker_reset")
}
