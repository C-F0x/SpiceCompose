package org.cf0x.spicecompose.data

import kotlinx.serialization.Serializable

@Serializable
data class CardConfig(
    val id: String,
    val name: String,
    val cardId: String,
    val idTrigger: String = "",
    val active: Boolean = false
)
