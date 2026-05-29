package org.cf0x.spicecompose.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.json.Json

class ServerRepository(private val settings: Settings = Settings()) {
    private val key = "servers_list"

    fun getServers(): List<ServerConfig> {
        val json = settings.getStringOrNull(key) ?: return emptyList()
        return try {
            Json.decodeFromString<List<ServerConfig>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveServers(servers: List<ServerConfig>) {
        val json = Json.encodeToString<List<ServerConfig>>(servers)
        settings[key] = json
    }

    fun addServer(server: ServerConfig) {
        val servers = getServers().toMutableList()
        servers.add(server)
        saveServers(servers)
    }

    fun deleteServer(id: String) {
        val servers = getServers().filter { it.id != id }
        saveServers(servers)
    }
}
