package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import org.cf0x.spicecompose.network.SpiceClient

suspend fun SpiceClient.cardInsert(unit: Int, cardID: String) {
    request(
        "card",
        "insert",
        listOf(JsonPrimitive(unit), JsonPrimitive(cardID))
    )
}
