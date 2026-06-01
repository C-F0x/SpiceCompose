package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

data class LightState(
    val name: String,
    var state: Double,
    val active: Boolean = false
)

suspend fun SpiceConnection.lightsRead(): List<LightState> {
    val req = SpiceRequest(module = "lights", function = "read")
    val res = request(req)
    return res.data.map {
        val arr = it.jsonArray
        LightState(
            name = arr[0].jsonPrimitive.content,
            state = arr[1].jsonPrimitive.double,
            active = arr[2].jsonPrimitive.boolean
        )
    }
}

suspend fun SpiceConnection.lightsWrite(states: List<LightState>) {
    if (states.isEmpty()) return
    val params = states.map {
        buildJsonArray {
            add(it.name)
            add(it.state)
        }
    }
    val req = SpiceRequest(module = "lights", function = "write", params = params)
    request(req)
}

suspend fun SpiceConnection.lightsWriteReset(names: List<String>) {
    val params = names.map { JsonPrimitive(it) }
    val req = SpiceRequest(module = "lights", function = "write_reset", params = params)
    request(req)
}
