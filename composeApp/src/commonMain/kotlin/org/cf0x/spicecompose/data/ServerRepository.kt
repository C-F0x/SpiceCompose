package org.cf0x.spicecompose.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.json.Json

class ServerRepository(private val settings: Settings = Settings()) {
    private val keyList = "servers_list"
    private val keyChosen = "chosen_server_id"

    var chosenServerId: String?
        get() = settings.getStringOrNull(keyChosen)
        set(value) { if (value == null) settings.remove(keyChosen) else settings[keyChosen] = value }

    fun getServers(): List<ServerConfig> {
        val json = settings.getStringOrNull(keyList) ?: return emptyList()
        return try {
            Json.decodeFromString<List<ServerConfig>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveServers(servers: List<ServerConfig>) {
        val json = Json.encodeToString<List<ServerConfig>>(servers)
        settings[keyList] = json
    }

    fun addServer(server: ServerConfig) {
        val servers = getServers().toMutableList()
        servers.removeAll { it.id == server.id }
        servers.add(server)
        saveServers(servers)
    }

    fun deleteServer(id: String) {
        val servers = getServers().filter { it.id != id }
        saveServers(servers)
        if (chosenServerId == id) chosenServerId = null
    }
}
