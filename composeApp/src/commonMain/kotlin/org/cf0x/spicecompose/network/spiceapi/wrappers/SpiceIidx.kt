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

suspend fun SpiceClient.iidxTapeledGet(vararg names: String): Map<String, List<Int>> {
    val params = names.map { JsonPrimitive(it) }
    val res = request("iidx", "tapeled_get", params)
    return res.jsonObject["data"]?.jsonObject?.mapValues { (_, v) ->
        v.jsonArray.map { it.jsonPrimitive.content.toInt() }
    } ?: emptyMap()
}
