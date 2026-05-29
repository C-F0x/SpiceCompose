package org.cf0x.spicecompose.network

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection

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
    
    private var connection: SpiceConnection? = null
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun connect(server: ServerConfig) {
        scope.launch {
            try {
                _status.value = ConnectionStatus.Connecting
                _currentServer.value = server
                _error.value = null
                
                connection?.dispose()
                
                val newConn = SpiceConnection(server.host, server.port, server.password)
                newConn.connect()
                
                // Simple check if it's actually alive if needed, 
                // but connect() should throw if failed.
                connection = newConn
                _status.value = ConnectionStatus.Connected
            } catch (e: Exception) {
                _status.value = ConnectionStatus.Error
                _error.value = e.message ?: "Unknown connection error"
                _currentServer.value = null
            }
        }
    }

    fun disconnect() {
        scope.launch {
            connection?.dispose()
            connection = null
            _status.value = ConnectionStatus.Disconnected
            _currentServer.value = null
        }
    }
    
    fun getConnection(): SpiceConnection? = if (status.value == ConnectionStatus.Connected) connection else null
}

val LocalConnectionManager = compositionLocalOf<ConnectionManager> {
    error("No ConnectionManager provided")
}
