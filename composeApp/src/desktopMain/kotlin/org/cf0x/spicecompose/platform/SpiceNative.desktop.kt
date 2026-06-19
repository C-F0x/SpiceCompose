package org.cf0x.spicecompose.platform

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

actual object SpiceNative {
    private const val BASE_URL = "http://127.0.0.1:9800"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
    }

    @Serializable
    data class ConnectBody(val host: String, val port: Int, val password: String)

    @Serializable
    data class RequestBody(val module: String, val function: String, val params: JsonArray)

    @Serializable
    data class StatusResponse(val connected: Boolean)

    actual suspend fun connect(host: String, port: Int, password: String): Boolean {
        return try {
            val response: StatusResponse = client.post("$BASE_URL/connect") {
                contentType(ContentType.Application.Json)
                setBody(ConnectBody(host, port, password))
            }.body()
            response.connected
        } catch (_: Exception) {
            false
        }
    }

    actual suspend fun request(module: String, function: String, paramsJson: String): String {
        val params = Json.parseToJsonElement(paramsJson) as JsonArray
        return try {
            val response: JsonElement = client.post("$BASE_URL/request") {
                contentType(ContentType.Application.Json)
                setBody(RequestBody(module, function, params))
            }.body()
            response.toString()
        } catch (e: Exception) {
            """{"error":"${e.message}"}"""
        }
    }

    actual suspend fun disconnect() {
        try { client.post("$BASE_URL/disconnect") } catch (_: Exception) {}
    }
}
