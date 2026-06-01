package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

data class TouchState(
    val id: Int,
    var x: Int,
    var y: Int,
    var active: Boolean = true,
    var updated: Boolean = true
)

suspend fun SpiceConnection.touchRead(): List<TouchState> {
    val req = SpiceRequest(module = "touch", function = "read")
    val res = request(req)
    return res.data.map {
        val arr = it.jsonArray
        TouchState(
            id = arr[0].jsonPrimitive.int,
            x = arr[1].jsonPrimitive.int,
            y = arr[2].jsonPrimitive.int
        )
    }
}

suspend fun SpiceConnection.touchWrite(states: List<TouchState>) {
    if (states.isEmpty()) return
    val params = states.map {
        buildJsonArray {
            add(it.id)
            add(it.x)
            add(it.y)
        }
    }
    val req = SpiceRequest(module = "touch", function = "write", params = params)
    request(req)
}

suspend fun SpiceConnection.touchWriteReset(ids: List<Int>) {
    if (ids.isEmpty()) return
    val params = ids.map { JsonPrimitive(it) }
    val req = SpiceRequest(module = "touch", function = "write_reset", params = params)
    request(req)
}
