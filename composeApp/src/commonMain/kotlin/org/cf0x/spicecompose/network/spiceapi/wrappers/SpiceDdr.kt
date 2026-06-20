package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.cf0x.spicecompose.network.SpiceClient

/**
 * DDR (DanceDanceRevolution) gold cabinet tape LED readout.
 *
 * Returns a map of device name → RGB values.
 * 11 devices: p1_foot_up/down/left/right, p2_foot_up/down/left/right,
 *             top_panel, monitor_left, monitor_right.
 */
suspend fun SpiceClient.ddrTapeledGet(): Map<String, List<Int>> {
    val res = request("ddr", "tapeled_get")
    return res.jsonObject["data"]?.jsonObject?.mapValues { (_, v) ->
        v.jsonArray.map { it.jsonPrimitive.content.toInt() }
    } ?: emptyMap()
}
