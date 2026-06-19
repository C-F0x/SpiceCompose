package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

data class ButtonState(
    val name: String,
    val state: Double,
    val active: Boolean = false
)

suspend fun SpiceClient.buttonsRead(): List<ButtonState> {
    val res = request("buttons", "read")
    val data = res.jsonObject["data"]?.jsonArray ?: return emptyList()
    return data.map {
        val arr = it.jsonArray
        ButtonState(
            name = arr[0].jsonPrimitive.content,
            state = arr[1].jsonPrimitive.double,
            active = arr[2].jsonPrimitive.boolean
        )
    }
}

suspend fun SpiceClient.buttonsWrite(states: List<ButtonState>) {
    val params = states.map {
        buildJsonArray {
            add(it.name)
            add(it.state)
        }
    }
    request("buttons", "write", params)
}

suspend fun SpiceClient.buttonsWriteReset(names: List<String>) {
    val params = names.map { JsonPrimitive(it) }
    request("buttons", "write_reset", params)
}
