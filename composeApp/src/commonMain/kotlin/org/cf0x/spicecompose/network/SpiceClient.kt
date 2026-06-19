package org.cf0x.spicecompose.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.cf0x.spicecompose.platform.SpiceNative

/**
 * Typed SPICE client — wraps [SpiceNative] (JNI / HTTP / subprocess)
 * and exposes a clean Kotlin API.
 */
class SpiceClient {

    // ── /connect ──

    @Serializable
    data class StatusResponse(
        val connected: Boolean,
        val host: String? = null,
        val port: Int? = null
    )

    suspend fun connect(host: String, port: Int, password: String): StatusResponse {
        val ok = SpiceNative.connect(host, port, password)
        return StatusResponse(connected = ok, host = host.takeIf { ok }, port = port.takeIf { ok })
    }

    suspend fun disconnect(): StatusResponse {
        SpiceNative.disconnect()
        return StatusResponse(connected = false)
    }

    // ── /request ──

    suspend fun request(module: String, function: String, params: List<JsonElement> = emptyList()): JsonElement {
        val paramsJson = Json.encodeToString(JsonArray(params))
        val raw = SpiceNative.request(module, function, paramsJson)
        val response = Json.parseToJsonElement(raw)
        val obj = response.jsonObject

        // Backend-level error (transport failure, timeout, etc.)
        val backendError = obj["error"]?.jsonPrimitive?.content
        if (!backendError.isNullOrEmpty()) {
            throw Exception("Backend error: $backendError")
        }

        // SPICE-level errors from the device
        val errors = obj["errors"]?.jsonArray
        if (errors != null && errors.isNotEmpty()) {
            throw Exception("SPICE error: $errors")
        }

        return response
    }

    suspend fun close() {
        SpiceNative.disconnect()
    }
}
