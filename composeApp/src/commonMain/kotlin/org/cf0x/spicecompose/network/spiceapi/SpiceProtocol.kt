package org.cf0x.spicecompose.network.spiceapi

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.random.Random

private var lastId = Random.nextInt(1, 100000)

@Serializable
data class SpiceRequest(
    val id: Int = ++lastId,
    val module: String,
    val function: String,
    val params: List<JsonElement> = emptyList()
)

@Serializable
data class SpiceResponse(
    val id: Int,
    val errors: List<String> = emptyList(),
    val data: List<JsonElement> = emptyList()
)

val spiceJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
