package org.cf0x.spicecompose.network

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private var heartbeatJob: Job? = null
    private var connectJob: Job? = null

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** One-shot toast messages for connect/disconnect events. */
    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun connect(server: ServerConfig) {
        println("[SpiceCompose] connect → ${server.host}:${server.port}")
        connectJob?.cancel()
        connectJob = scope.launch {
            try {
                _status.value = ConnectionStatus.Connecting
                _currentServer.value = server
                _error.value = null

                client?.close()
                val newClient = SpiceClient()
                // TCP connect (≤3s) + SPICE session verification (≤3s) inside the
                // Rust backend; give the Kotlin side generous headroom.
                withTimeout(12_000) {
                    val result = newClient.connect(server.host, server.port, server.password)
                    if (!result.connected) throw Exception("Connection refused")
                }
                if (!isActive) return@launch

                client = newClient
                _status.value = ConnectionStatus.Connected
                println("[SpiceCompose] connect OK ← ${server.host}:${server.port}")
                startHeartbeat()
            } catch (e: CancellationException) {
                // Disconnect/teardown cancelled this attempt — propagate, no toast.
                throw e
            } catch (e: Exception) {
                _status.value = ConnectionStatus.Disconnected
                val reason = when {
                    e is TimeoutCancellationException -> "connection_timeout"
                    e.message == "Connection refused" -> "connection_refused"
                    else -> e.message ?: "unknown_error"
                }
                println("[SpiceCompose] connect FAILED: $reason")
                _error.value = reason
                _toastMessage.tryEmit(reason)
                _currentServer.value = null
                client?.close(); client = null
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && _status.value == ConnectionStatus.Connected) {
                delay(5_000)
                try {
                    client?.request("info", "avs")
                } catch (_: Exception) {
                    heartbeatFailed("server_died")
                    return@launch
                }
            }
        }
    }

    private fun heartbeatFailed(reason: String) {
        println("[SpiceCompose] heartbeat DEAD: $reason")
        heartbeatJob?.cancel()
        scope.launch {
            client?.close()
            client = null
            _status.value = ConnectionStatus.Disconnected
            _currentServer.value = null
            _toastMessage.tryEmit("heartbeat: ${reason}")
        }
    }

    fun disconnect() {
        println("[SpiceCompose] disconnect")
        heartbeatJob?.cancel()
        scope.launch {
            client?.close()
            client = null
            _status.value = ConnectionStatus.Disconnected
            _currentServer.value = null
            _toastMessage.tryEmit("disconnected")
        }
    }

    fun getClient(): SpiceClient? = client
}

val LocalConnectionManager = compositionLocalOf<ConnectionManager> {
    error("No ConnectionManager provided")
}
