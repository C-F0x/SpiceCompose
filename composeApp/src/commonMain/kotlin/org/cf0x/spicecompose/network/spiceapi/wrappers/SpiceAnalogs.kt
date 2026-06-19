package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

data class AnalogState(
    val name: String,
    var state: Double,
    val active: Boolean = false
)

suspend fun SpiceClient.analogsRead(): List<AnalogState> {
    val res = request("analogs", "read")
    val data = res.jsonObject["data"]?.jsonArray ?: return emptyList()
    return data.map {
        val arr = it.jsonArray
        AnalogState(
            name = arr[0].jsonPrimitive.content,
            state = arr[1].jsonPrimitive.double,
            active = arr[2].jsonPrimitive.boolean
        )
    }
}

suspend fun SpiceClient.analogsWrite(states: List<AnalogState>) {
    if (states.isEmpty()) return
    val params = states.map {
        buildJsonArray {
            add(it.name)
            add(it.state)
        }
    }
    request("analogs", "write", params)
}

suspend fun SpiceClient.analogsWriteReset(names: List<String>) {
    val params = names.map { JsonPrimitive(it) }
    request("analogs", "write_reset", params)
}
