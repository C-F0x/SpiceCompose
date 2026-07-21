package org.cf0x.spicecompose.ui.screen.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * General Slider for Analogs/Lights
 *
 * Frame counter throttling, throttleSkip is interval time, e.g throttleSkip = 2 aka.3 frame per send ≈ 50ms @60fps
 *
 * @param T   AnalogState / LightState
 * @param nameSelector
 * @param writeBlock
 */
class SliderWriteController<T>(
    private val nameSelector: (T) -> String,
    private val writeBlock: suspend (T) -> Unit,
) {
    private val counters = mutableMapOf<String, Int>()
    private val throttleSkip = 2

    fun write(item: T, scope: CoroutineScope) {
        val name = nameSelector(item)
        val cnt = (counters[name] ?: 0) + 1
        counters[name] = cnt
        if (cnt % (throttleSkip + 1) == 0) {
            scope.launch { writeBlock(item) }
        }
    }

    fun commit(item: T, scope: CoroutineScope) {
        counters.remove(nameSelector(item))
        scope.launch { writeBlock(item) }
    }
}
