package org.cf0x.spicecompose.ui.screen.utils.subscreen

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*

/**
 * Capture2x WebSocket client.
 *
 * WebSocket port = API port + 1.
 * Protocol: JSON commands sent as binary frames (RC4 encrypted when password is set).
 * Incoming: capture2x binary frames (opcode 0x02, unencrypted, 20-byte header) + API
 * responses (encrypted binary when password is set).
 *
 * Distinction: capture2x frames always start with 0x00 or 0x01.
 */
class Capture2xConnection(
    private val host: String,
    private val apiPort: Int,
    private val password: String = "",
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val client = HttpClient { install(WebSockets) }
    private var wsJob: Job? = null

    // RC4 cipher for encryption (shared across send/recv, like Spice2x)
    private var cipher: Rc4Cipher? = if (password.isNotEmpty()) Rc4Cipher(password.encodeToByteArray()) else null

    private val _frames = MutableSharedFlow<Rgb24Image>(replay = 0, extraBufferCapacity = 4)
    val frames: SharedFlow<Rgb24Image> = _frames.asSharedFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _error = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val error: SharedFlow<String> = _error.asSharedFlow()

    data class FrameHeader(
        val frameType: Int, val divide: Int, val fps: Int, val compression: Int,
        val timestamp: Long, val width: Int, val height: Int, val dataSize: Int
    )

    companion object {
        private const val HEADER_SIZE = 20
        private const val FRAME_TYPE_KEYFRAME = 0x00

        fun parseHeader(data: ByteArray): FrameHeader? {
            if (data.size < HEADER_SIZE) return null
            return FrameHeader(
                frameType = data[0].toInt() and 0xFF,
                divide = data[1].toInt() and 0xFF,
                fps = data[2].toInt() and 0xFF,
                compression = data[3].toInt() and 0xFF,
                timestamp = (data[4].toLong() and 0xFF) or
                        ((data[5].toLong() and 0xFF) shl 8) or
                        ((data[6].toLong() and 0xFF) shl 16) or
                        ((data[7].toLong() and 0xFF) shl 24) or
                        ((data[8].toLong() and 0xFF) shl 32) or
                        ((data[9].toLong() and 0xFF) shl 40) or
                        ((data[10].toLong() and 0xFF) shl 48) or
                        ((data[11].toLong() and 0xFF) shl 56),
                width = (data[12].toInt() and 0xFF) or ((data[13].toInt() and 0xFF) shl 8),
                height = (data[14].toInt() and 0xFF) or ((data[15].toInt() and 0xFF) shl 8),
                dataSize = (data[16].toInt() and 0xFF) or
                        ((data[17].toInt() and 0xFF) shl 8) or
                        ((data[18].toInt() and 0xFF) shl 16) or
                        ((data[19].toInt() and 0xFF) shl 24)
            )
        }
    }

    suspend fun connect(
        screen: Int = 0, divide: Int = 1, fps: Int = 60,
        timeoutMs: Long = 8000
    ): Boolean {
        wsJob?.cancel(); wsJob = null
        cipher = if (password.isNotEmpty()) Rc4Cipher(password.encodeToByteArray()) else null

        val wsUrl = "ws://$host:${apiPort + 1}/"
        println("[Capture2x] Connecting to $wsUrl (password=${password.isNotEmpty()})")

        val connectedSignal = CompletableDeferred<Boolean>()

        wsJob = scope.launch {
            try {
                client.webSocket(urlString = wsUrl) {
                    connectedSignal.complete(true)
                    _connected.value = true
                    println("[Capture2x] WebSocket connected OK")

                    // Send subscribe as binary frame (encrypted if needed)
                    sendBinary(buildSubscribeJson(screen, divide, fps))
                    println("[Capture2x] Sent subscribe command")

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Binary -> {
                                val data = frame.data
                                if (data.isEmpty()) continue

                                // capture2x binary frame (unencrypted, starts with 0x00)
                                val firstByte = data[0].toInt() and 0xFF
                                if (firstByte == FRAME_TYPE_KEYFRAME) {
                                    // capture2x binary frame (unencrypted)
                                    if (data.size < HEADER_SIZE) continue
                                    val header = parseHeader(data) ?: continue
                                    val payload = data.copyOfRange(HEADER_SIZE, data.size)
                                    val decoded = processFrame(header, payload)
                                    if (decoded != null) _frames.tryEmit(decoded)
                                } else {
                                    // API response — decrypt if needed, then parse JSON
                                    val decrypted = data.copyOf()
                                    cipher?.crypt(decrypted)
                                    val text = decrypted.decodeToString().trimEnd('\u0000')
                                    println("[Capture2x] API response: $text")
                                }
                            }
                            is Frame.Text -> {
                                println("[Capture2x] Text frame: ${frame.readText()}")
                            }
                            else -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                println("[Capture2x] WS error: ${e::class.simpleName} → ${e.message}")
                connectedSignal.complete(false)
                _connected.value = false
                _error.tryEmit(e.message ?: "WebSocket error")
            } finally {
                _connected.value = false
            }
        }

        return try {
            withTimeout(timeoutMs) {
                val result = connectedSignal.await()
                println("[Capture2x] connect() returning: $result")
                result
            }
        } catch (e: TimeoutCancellationException) {
            println("[Capture2x] Connection TIMEOUT after ${timeoutMs}ms")
            wsJob?.cancel()
            _connected.value = false
            _error.tryEmit("Connection timeout")
            false
        }
    }

    /** Send JSON command as binary frame (encrypted if cipher is set). */
    private suspend fun WebSocketSession.sendBinary(json: String) {
        val raw = (json + "\u0000").encodeToByteArray()  // null-terminated
        cipher?.crypt(raw)
        send(Frame.Binary(true, raw))
    }

    private var requestId = 1

    private fun buildSubscribeJson(screen: Int, divide: Int, fps: Int) =
        buildJsonObject {
            put("id", JsonPrimitive(requestId++))
            put("module", JsonPrimitive("capture2x"))
            put("function", JsonPrimitive("subscribe"))
            put("params", JsonArray(listOf(
                JsonPrimitive(screen), JsonPrimitive(divide), JsonPrimitive(fps)
            )))
        }.toString()

    private fun processFrame(header: FrameHeader, payload: ByteArray): Rgb24Image? = try {
        QoiDecoder.decodeToRgb24(payload)
    } catch (e: Exception) {
        println("[Capture2x] Frame decode error: ${e.message}"); null
    }

    suspend fun disconnect() {
        wsJob?.cancel(); wsJob = null
        _connected.value = false
        client.close()
    }
}
