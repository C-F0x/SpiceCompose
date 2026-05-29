package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

data class ButtonState(
    val name: String,
    val state: Double,
    val active: Boolean = false
)

suspend fun SpiceConnection.buttonsRead(): List<ButtonState> {
    val req = SpiceRequest(module = "buttons", function = "read")
    val res = request(req)
    return res.data.map {
        val arr = it.jsonArray
        ButtonState(
            name = arr[0].jsonPrimitive.content,
            state = arr[1].jsonPrimitive.double,
            active = arr[2].jsonPrimitive.boolean
        )
    }
}

suspend fun SpiceConnection.buttonsWrite(states: List<ButtonState>) {
    val params = states.map {
        buildJsonArray {
            add(it.name)
            add(it.state)
        }
    }
    val req = SpiceRequest(module = "buttons", function = "write", params = params)
    request(req)
}

suspend fun SpiceConnection.buttonsWriteReset(names: List<String>) {
    val params = names.map { JsonPrimitive(it) }
    val req = SpiceRequest(module = "buttons", function = "write_reset", params = params)
    request(req)
}
