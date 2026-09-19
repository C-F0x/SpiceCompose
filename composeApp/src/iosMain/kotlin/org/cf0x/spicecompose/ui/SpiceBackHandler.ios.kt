package org.cf0x.spicecompose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState

private object IOSBackDispatcher {
    private val handlers = mutableListOf<() -> Unit>()

    fun dispatch(): Boolean {
        val handler = handlers.lastOrNull() ?: return false
        handler()
        return true
    }

    fun add(handler: () -> Unit) {
        handlers += handler
    }

    fun remove(handler: () -> Unit) {
        handlers.remove(handler)
    }
}

/** Called by the native iOS edge-swipe gesture. */
fun dispatchIOSBack(): Boolean = IOSBackDispatcher.dispatch()

@Composable
actual fun SpiceBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentOnBack = rememberUpdatedState(onBack)

    DisposableEffect(enabled) {
        if (!enabled) {
            onDispose {}
        } else {
            val handler = { currentOnBack.value() }
            IOSBackDispatcher.add(handler)
            onDispose { IOSBackDispatcher.remove(handler) }
        }
    }
}
