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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** One-shot toast messages for connect/disconnect events. */
    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun connect(server: ServerConfig) {
        scope.launch {
            try {
                _status.value = ConnectionStatus.Connecting
                _currentServer.value = server
                _error.value = null

                client?.close()
                val newClient = SpiceClient()
                withTimeout(5000) {
                    val result = newClient.connect(server.host, server.port, server.password)
                    if (!result.connected) throw Exception("Connection refused")
                }

                client = newClient
                _status.value = ConnectionStatus.Connected
                _toastMessage.tryEmit("已连接")
                startHeartbeat()
            } catch (e: Exception) {
                _status.value = ConnectionStatus.Disconnected
                val reason = when {
                    e is TimeoutCancellationException -> "连接超时"
                    e.message == "Connection refused" -> "连接被拒绝"
                    else -> e.message ?: "未知错误"
                }
                _error.value = reason
                _toastMessage.tryEmit("已断开，${reason}")
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
                    val info = client?.request("info", "avs")
                    if (info == null) {
                        heartbeatFailed("服务端死亡")
                        return@launch
                    }
                } catch (_: Exception) {
                    heartbeatFailed("服务端死亡")
                    return@launch
                }
            }
        }
    }

    private fun heartbeatFailed(reason: String) {
        heartbeatJob?.cancel()
        scope.launch {
            client?.close()
            client = null
            _status.value = ConnectionStatus.Disconnected
            _currentServer.value = null
            _toastMessage.tryEmit("已断开，${reason}")
        }
    }

    fun disconnect() {
        heartbeatJob?.cancel()
        scope.launch {
            client?.close()
            client = null
            _status.value = ConnectionStatus.Disconnected
            _currentServer.value = null
            _toastMessage.tryEmit("已断开")
        }
    }

    fun getClient(): SpiceClient? =
        if (status.value == ConnectionStatus.Connected) client else null
}

val LocalConnectionManager = compositionLocalOf<ConnectionManager> {
    error("No ConnectionManager provided")
}
