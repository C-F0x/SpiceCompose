package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.SpiceClient

data class CaptureData(
    val timestamp: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val data: ByteArray = byteArrayOf()
)

suspend fun SpiceClient.captureGetScreens(): List<Int> {
    val res = request("capture", "get_screens")
    return res.jsonObject["data"]?.jsonArray?.map { it.jsonPrimitive.int } ?: emptyList()
}

suspend fun SpiceClient.captureGetJPG(
    screen: Int = 0,
    quality: Int = 60,
    divide: Int = 1
): CaptureData {
    val res = request(
        "capture",
        "get_jpg",
        listOf(JsonPrimitive(screen), JsonPrimitive(quality), JsonPrimitive(divide))
    )
    val d = res.jsonObject["data"]?.jsonArray ?: return CaptureData()
    if (d.size < 4) return CaptureData()
    
    return CaptureData(
        timestamp = d[0].jsonPrimitive.long,
        width = d[1].jsonPrimitive.int,
        height = d[2].jsonPrimitive.int,
        data = decodeBase64(d[3].jsonPrimitive.content)
    )
}

expect fun decodeBase64(base64: String): ByteArray
