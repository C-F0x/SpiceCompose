package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.*
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

data class CaptureData(
    val timestamp: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val data: ByteArray = byteArrayOf()
)

suspend fun SpiceConnection.captureGetScreens(): List<Int> {
    val req = SpiceRequest(module = "capture", function = "get_screens")
    val res = request(req)
    return res.data.map { it.jsonPrimitive.int }
}

suspend fun SpiceConnection.captureGetJPG(
    screen: Int = 0,
    quality: Int = 60,
    divide: Int = 1
): CaptureData {
    val req = SpiceRequest(
        module = "capture",
        function = "get_jpg",
        params = listOf(JsonPrimitive(screen), JsonPrimitive(quality), JsonPrimitive(divide))
    )
    val res = request(req)
    val d = res.data
    if (d.size < 4) return CaptureData()
    
    return CaptureData(
        timestamp = d[0].jsonPrimitive.long,
        width = d[1].jsonPrimitive.int,
        height = d[2].jsonPrimitive.int,
        data = decodeBase64(d[3].jsonPrimitive.content)
    )
}

expect fun decodeBase64(base64: String): ByteArray
