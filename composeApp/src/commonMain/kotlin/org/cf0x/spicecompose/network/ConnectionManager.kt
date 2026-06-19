package org.cf0x.spicecompose.network

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cf0x.spicecompose.data.ServerConfig

enum class ConnectionStatus {
    Disconnected,
    Connecting,
    Connected,
    Error
}

class ConnectionManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _status = MutableStateFlow(ConnectionStatus.Disconnected)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _currentServer = MutableStateFlow<ServerConfig?>(null)
    val currentServer: StateFlow<ServerConfig?> = _currentServer.asStateFlow()

    private var client: SpiceClient? = null

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun connect(server: ServerConfig) {
        scope.launch {
            try {
                _status.value = ConnectionStatus.Connecting
                _currentServer.value = server
                _error.value = null

                client?.close()

                val newClient = SpiceClient()
                withTimeout(5000) {
                    newClient.connect(server.host, server.port, server.password)
                }

                client = newClient
                _status.value = ConnectionStatus.Connected
            } catch (e: Exception) {
                _status.value = ConnectionStatus.Disconnected
                _error.value = if (e is TimeoutCancellationException) "Connection timed out"
                    else (e.message ?: "Unknown error")
                _currentServer.value = null
                client?.close()
                client = null
            }
        }
    }

    fun disconnect() {
        scope.launch {
            client?.close()
            client = null
            _status.value = ConnectionStatus.Disconnected
            _currentServer.value = null
        }
    }

    fun getClient(): SpiceClient? =
        if (status.value == ConnectionStatus.Connected) client else null
}

val LocalConnectionManager = compositionLocalOf<ConnectionManager> {
    error("No ConnectionManager provided")
}
