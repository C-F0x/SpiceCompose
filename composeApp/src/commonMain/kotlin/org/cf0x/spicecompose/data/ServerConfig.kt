package org.cf0x.spicecompose.data

import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 673,
    val password: String = ""
)
