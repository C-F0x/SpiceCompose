package org.cf0x.spicecompose.platform

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

actual object SpiceNative {
    private const val BASE_URL = "http://127.0.0.1:9800"
    private val json = Json { encodeDefaults = true }

    @Serializable data class ConnectReq(val host: String, val port: Int, val password: String)
    @Serializable data class RequestReq(val module: String, val function: String, val params: JsonArray)

    actual suspend fun connect(host: String, port: Int, password: String): Boolean = try {
        val body = json.encodeToString(ConnectReq(host, port, password))
        val resp = fetchPost("$BASE_URL/connect", body)
        val result = resp.json().await()
        result?.unsafeCast<ConnectResult>()?.connected ?: false
    } catch (_: Exception) { false }

    actual suspend fun request(module: String, function: String, paramsJson: String): String = try {
        val params = json.parseToJsonElement(paramsJson) as JsonArray
        val body = json.encodeToString(RequestReq(module, function, params))
        val resp = fetchPost("$BASE_URL/request", body)
        val text = resp.text().await()
        // text() returns a Promise-based type; .toString() handles various return types
        text.toString()
    } catch (e: Exception) { """{"error":"${e.message}"}""" }

    actual suspend fun disconnect() {
        try { fetchPost("$BASE_URL/disconnect", "{}") } catch (_: Exception) {}
    }

    private suspend fun fetchPost(url: String, body: String): org.w3c.fetch.Response {
        val init = org.w3c.fetch.RequestInit()
        init.method = "POST"
        init.headers = org.w3c.fetch.Headers().also { it.append("Content-Type", "application/json") }
        init.body = body.toJsString()
        return window.fetch(url, init).await()
    }
}

private external interface ConnectResult : kotlin.js.JsAny {
    val connected: Boolean
}
