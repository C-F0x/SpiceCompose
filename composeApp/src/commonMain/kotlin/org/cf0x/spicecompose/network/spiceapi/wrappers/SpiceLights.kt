package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

data class LightState(
    val name: String,
    var state: Double,
    val active: Boolean = false
)

/**
 * 游戏专用 RGB LED 灯带/设备数据。
 * [leds] 中的每个元素是 [R, G, B] 三元组（0-255）。
 */
data class GameLedDevice(
    val name: String,
    val leds: List<List<Int>>
)

/** model → 游戏类型，用于 tapeled_get 路由。 */
private val gameLedModels: Map<String, String> = mapOf(
    "JDX" to "ddr", "KDX" to "ddr", "MDX" to "ddr", "TDX" to "ddr",
    "JDZ" to "iidx", "KDZ" to "iidx", "LDJ" to "iidx", "TDJ" to "iidx",
    "REC" to "drs",
)

/**
 * 根据 [model]（如 "LDJ"、"KDX"）自动路由到对应游戏的 tapeled_get。
 * 返回 null 表示该游戏没有 LED 灯带数据。
 */
suspend fun SpiceClient.lightsReadGameSpecific(model: String): List<GameLedDevice>? {
    return when (gameLedModels[model]) {
        "ddr" -> ddrTapeledGet().map { (name, rgb) ->
            GameLedDevice(name, rgb.chunked(3))
        }
        "iidx" -> iidxTapeledGet().map { (name, rgb) ->
            GameLedDevice(name, rgb.chunked(3))
        }
        "drs" -> {
            val raw = drsTapeledGet()
            listOf(GameLedDevice("DRS Strip", raw.chunked(3)))
        }
        else -> null
    }
}

suspend fun SpiceClient.lightsRead(vararg names: String): List<LightState> {
    val res = if (names.isEmpty()) {
        request("lights", "read")
    } else {
        request("lights", "read", names.map { JsonPrimitive(it) })
    }
    val data = res.jsonObject["data"]?.jsonArray ?: return emptyList()
    return data.map {
        val arr = it.jsonArray
        LightState(
            name = arr[0].jsonPrimitive.content,
            state = arr[1].jsonPrimitive.double,
            active = arr[2].jsonPrimitive.boolean
        )
    }
}

suspend fun SpiceClient.lightsWrite(states: List<LightState>) {
    if (states.isEmpty()) return
    val params = states.map {
        buildJsonArray {
            add(it.name)
            add(it.state)
        }
    }
    request("lights", "write", params)
}

suspend fun SpiceClient.lightsWriteReset(names: List<String>) {
    val params = names.map { JsonPrimitive(it) }
    request("lights", "write_reset", params)
}
