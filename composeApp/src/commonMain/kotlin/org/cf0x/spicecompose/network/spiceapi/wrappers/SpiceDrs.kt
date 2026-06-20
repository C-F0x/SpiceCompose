package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.cf0x.spicecompose.network.SpiceClient

/** DRS tape LED raw byte array. */
suspend fun SpiceClient.drsTapeledGet(): List<Int> {
    val res = request("drs", "tapeled_get")
    return res.jsonObject["data"]?.jsonArray?.map { it.jsonPrimitive.content.toInt() } ?: emptyList()
}

data class DrsTouchEvent(
    val type: Int,
    val id: Int,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

/** Inject touch events into DRS with full geometry. */
suspend fun SpiceClient.drsTouchSet(events: List<DrsTouchEvent>) {
    val params = events.map { event ->
        buildJsonArray {
            add(JsonPrimitive(event.type))
            add(JsonPrimitive(event.id))
            add(JsonPrimitive(event.x))
            add(JsonPrimitive(event.y))
            add(JsonPrimitive(event.width))
            add(JsonPrimitive(event.height))
        }
    }
    request("drs", "touch_set", params)
}
