package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

data class AnalogState(
    val name: String,
    var state: Double,
    val active: Boolean = false
)

suspend fun SpiceConnection.analogsRead(): List<AnalogState> {
    val req = SpiceRequest(module = "analogs", function = "read")
    val res = request(req)
    return res.data.map {
        val arr = it.jsonArray
        AnalogState(
            name = arr[0].jsonPrimitive.content,
            state = arr[1].jsonPrimitive.double,
            active = arr[2].jsonPrimitive.boolean
        )
    }
}

suspend fun SpiceConnection.analogsWrite(states: List<AnalogState>) {
    if (states.isEmpty()) return
    val params = states.map {
        buildJsonArray {
            add(it.name)
            add(it.state)
        }
    }
    val req = SpiceRequest(module = "analogs", function = "write", params = params)
    request(req)
}

suspend fun SpiceConnection.analogsWriteReset(names: List<String>) {
    val params = names.map { JsonPrimitive(it) }
    val req = SpiceRequest(module = "analogs", function = "write_reset", params = params)
    request(req)
}
