package org.cf0x.spicecompose.network.spiceapi

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlin.time.Duration.Companion.milliseconds

/**
 * SpiceConnection handles the low-level TCP communication with SpiceTools.
 */
expect class SpiceSocket() {
    suspend fun connect(host: String, port: Int, timeout: Long)
    suspend fun read(): ByteArray?
    suspend fun write(data: ByteArray)
    fun close()
}

class SpiceConnection(
    val host: String,
    val port: Int,
    private val pass: String
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var socket: SpiceSocket? = null
    private var cipher: RC4? = null
    private val writeMutex = Mutex()
    
    private val _responses = MutableSharedFlow<SpiceResponse>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    
    private var isDisposed = false

    init {
        if (pass.isNotEmpty()) {
            cipher = RC4(pass.encodeToByteArray())
        }
    }

    suspend fun connect() {
        val s = SpiceSocket()
        s.connect(host, port, 3000)
        socket = s
        
        scope.launch {
            val buffer = mutableListOf<Byte>()
            try {
                while (isActive && !isDisposed) {
                    val data = try {
                        s.read()
                    } catch (e: Exception) {
                        null
                    } ?: break
                    
                    // Decrypt
                    cipher?.crypt(data)
                    
                    // Buffer and parse
                    for (b in data) {
                        if (b == 0.toByte()) {
                            val msg = buffer.toByteArray().decodeToString()
                            buffer.clear()
                            try {
                                val res = spiceJson.decodeFromString<SpiceResponse>(msg)
                                _responses.emit(res)
                            } catch (e: Exception) {
                                // ignore malformed
                            }
                        } else {
                            buffer.add(b)
                        }
                    }
                }
            } finally {
                dispose()
            }
        }
    }

    @OptIn(FlowPreview::class)
    suspend fun request(req: SpiceRequest): SpiceResponse = writeMutex.withLock {
        val s = socket ?: throw Exception("Not connected")
        
        val json = spiceJson.encodeToString(req) + "\u0000"
        val data = json.encodeToByteArray()
        
        val encrypted = data.copyOf()
        cipher?.crypt(encrypted)
        
        val responseFlow = _responses.filter { it.id == req.id }
            .timeout(3000.milliseconds)
            
        try {
            s.write(encrypted)
        } catch (e: Exception) {
            throw Exception("Write error: ${e.message}")
        }
        
        val response = try {
            responseFlow.first()
        } catch (e: Exception) {
            throw Exception("Timeout or connection closed while waiting for response")
        }
        
        if (response.errors.isNotEmpty()) {
            throw Exception("API Error: ${response.errors.joinToString()}")
        }
        
        return response
    }

    fun dispose() {
        isDisposed = true
        socket?.close()
        scope.cancel()
    }
}
