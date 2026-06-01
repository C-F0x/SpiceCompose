package org.cf0x.spicecompose.network.spiceapi.wrappers

import kotlinx.serialization.json.JsonPrimitive
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.SpiceRequest

suspend fun SpiceConnection.cardInsert(unit: Int, cardID: String) {
    val req = SpiceRequest(
        module = "card",
        function = "insert",
        params = listOf(JsonPrimitive(unit), JsonPrimitive(cardID))
    )
    request(req)
}
