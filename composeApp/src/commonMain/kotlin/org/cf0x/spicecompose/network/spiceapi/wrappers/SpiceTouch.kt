package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

data class TouchState(
    val id: Int,
    var x: Int,
    var y: Int,
    var active: Boolean = true,
    var updated: Boolean = true
)

suspend fun SpiceClient.touchRead(): List<TouchState> {
    val res = request("touch", "read")
    val data = res.jsonObject["data"]?.jsonArray ?: return emptyList()
    return data.map {
        val arr = it.jsonArray
        TouchState(
            id = arr[0].jsonPrimitive.int,
            x = arr[1].jsonPrimitive.int,
            y = arr[2].jsonPrimitive.int
        )
    }
}

suspend fun SpiceClient.touchWrite(states: List<TouchState>) {
    if (states.isEmpty()) return
    val params = states.map {
        buildJsonArray {
            add(it.id)
            add(it.x)
            add(it.y)
        }
    }
    request("touch", "write", params)
}

suspend fun SpiceClient.touchWriteReset(ids: List<Int>) {
    if (ids.isEmpty()) return
    val params = ids.map { JsonPrimitive(it) }
    request("touch", "write_reset", params)
}
