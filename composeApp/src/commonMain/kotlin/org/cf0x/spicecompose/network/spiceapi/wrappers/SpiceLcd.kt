package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.cf0x.spicecompose.network.SpiceClient

/**
 * LCD touch-panel hardware diagnostics shared across games.
 */
data class LcdInfo(
    val enabled: Boolean,
    val csm: String,
    val brightness: Int,
    val contrast: Int,
    val backlight: Int,
    val red: Int,
    val green: Int,
    val blue: Int,
)

suspend fun SpiceClient.lcdInfo(): LcdInfo {
    val res = request("lcd", "info")
    val obj = res.jsonObject["data"]?.jsonObject ?: return LcdInfo(false, "", 0, 0, 0, 0, 0, 0)
    return LcdInfo(
        enabled    = obj["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
        csm        = obj["csm"]?.jsonPrimitive?.content ?: "",
        brightness = obj["bri"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
        contrast   = obj["con"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
        backlight  = obj["bl"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
        red        = obj["red"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
        green      = obj["green"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
        blue       = obj["blue"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
    )
}
