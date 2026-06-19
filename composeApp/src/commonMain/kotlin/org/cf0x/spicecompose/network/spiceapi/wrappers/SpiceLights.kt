package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

data class LightState(
    val name: String,
    var state: Double,
    val active: Boolean = false
)

suspend fun SpiceClient.lightsRead(): List<LightState> {
    val res = request("lights", "read")
    val data = res.jsonObject["data"]?.jsonArray ?: return emptyList()
    return data.map {
        val arr = it.jsonArray
        LightState(
            name = arr[0].jsonPrimitive.content,
            state = arr[1].jsonPrimitive.double,
            active = arr[2].jsonPrimitive.boolean
        )
    }
}

suspend fun SpiceClient.lightsWrite(states: List<LightState>) {
    if (states.isEmpty()) return
    val params = states.map {
        buildJsonArray {
            add(it.name)
            add(it.state)
        }
    }
    request("lights", "write", params)
}

suspend fun SpiceClient.lightsWriteReset(names: List<String>) {
    val params = names.map { JsonPrimitive(it) }
    request("lights", "write_reset", params)
}
