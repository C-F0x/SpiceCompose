package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import org.cf0x.spicecompose.network.SpiceClient

/**
 * Screen resize / multi-scene configuration control.
 * Allows a remote client to switch between pre-configured window layouts.
 */

suspend fun SpiceClient.imageResizeEnable(enable: Boolean) {
    request("resize", "image_resize_enable", listOf(JsonPrimitive(enable)))
}

/**
 * @param scene 0 = disable resize, 1–N = enable resize and select scene N-1.
 */
suspend fun SpiceClient.imageResizeSetScene(scene: Int) {
    request("resize", "image_resize_set_scene", listOf(JsonPrimitive(scene)))
}
