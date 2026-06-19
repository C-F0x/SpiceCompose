package org.cf0x.spicecompose.network

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.cf0x.spicecompose.network.spiceapi.wrappers.TouchState
import org.cf0x.spicecompose.network.spiceapi.wrappers.touchWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.touchWriteReset
import kotlin.random.Random

class TouchControl(private val connectionManager: ConnectionManager) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val touchStates = mutableListOf<TouchState>()
    private val mutex = Mutex()
    private var flushed = true
    private var writeCounter = 0
    private var curTouchID = 100000 + Random.nextInt(99999)

    private fun flushState() {
        val connection = connectionManager.getClient() ?: return
        
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
                if (updatedTouches.isNotEmpty()) connection.touchWrite(updatedTouches)
                if (inactiveTouches.isNotEmpty()) connection.touchWriteReset(inactiveTouches.map { it.id })
                
                mutex.withLock {
                    if (!flushed) flushState()
                }
            } catch (e: Exception) {
                // ignore
            } finally {
                writeCounter--
            }
        }
    }

    suspend fun touchDown(x: Int, y: Int): Int = mutex.withLock {
        val id = ++curTouchID
        val state = TouchState(id, x, y, active = true, updated = true)
        touchStates.add(state)
        flushed = false
        flushState()
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
