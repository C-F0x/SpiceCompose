package org.cf0x.spicecompose.network

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.cf0x.spicecompose.network.spiceapi.wrappers.TouchState
import org.cf0x.spicecompose.platform.SpiceNative
import org.cf0x.spicecompose.platform.maybeVibrate
import kotlin.random.Random

class TouchControl(private val connectionManager: ConnectionManager) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val touchStates = mutableListOf<TouchState>()
    private val mutex = Mutex()
    private var flushed = true
    private var writeCounter = 0
    private var curTouchID = 100000 + Random.nextInt(99999)

    private fun flushState() {
        if (connectionManager.getClient() == null) return
        
        scope.launch {
            mutex.withLock {
                if (flushed || touchStates.isEmpty() || writeCounter > 0) return@launch
            }
            
            val inactiveTouches = mutableListOf<TouchState>()
            val updatedTouches = mutableListOf<TouchState>()
            
            mutex.withLock {
                touchStates.forEach {
                    if (!it.active) inactiveTouches.add(it)
                    else if (it.updated) updatedTouches.add(it)
                }
                touchStates.removeAll { inactiveTouches.contains(it) }
                flushed = true
            }
            
            writeCounter++
            try {
                // Touch writes go through the dedicated touch connection
                // (SpiceNative.touchRequest) so they never queue behind
                // screen-polling requests on the main connection.
                if (updatedTouches.isNotEmpty()) {
                    val params = buildJsonArray {
                        updatedTouches.forEach {
                            add(buildJsonArray { add(JsonPrimitive(it.id)); add(JsonPrimitive(it.x)); add(JsonPrimitive(it.y)) })
                        }
                    }
                    SpiceNative.touchRequest("touch", "write", params.toString())
                }
                if (inactiveTouches.isNotEmpty()) {
                    val ids = buildJsonArray { inactiveTouches.forEach { add(JsonPrimitive(it.id)) } }
                    SpiceNative.touchRequest("touch", "write_reset", ids.toString())
                }
            } catch (e: Exception) {
                println("TouchControl flush error: ${e.message}")
            } finally {
                writeCounter--
            }
            mutex.withLock {
                if (!flushed) flushState()
            }
        }
    }

    suspend fun touchDown(x: Int, y: Int): Int = mutex.withLock {
        val id = ++curTouchID
        val state = TouchState(id, x, y, active = true, updated = true)
        touchStates.add(state)
        flushed = false
        flushState()
        maybeVibrate(30)
        id
    }

    suspend fun touchMove(id: Int, x: Int, y: Int) = mutex.withLock {
        val state = touchStates.find { it.id == id }
        if (state != null) {
            state.x = x
            state.y = y
            state.active = true
            state.updated = true
            flushed = false
            flushState()
        }
    }

    suspend fun touchUp(id: Int) = mutex.withLock {
        val state = touchStates.find { it.id == id }
        if (state != null) {
            state.active = false
            state.updated = true
            flushed = false
            flushState()
        }
    }
}
