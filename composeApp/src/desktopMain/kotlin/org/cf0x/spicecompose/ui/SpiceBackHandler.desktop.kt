package org.cf0x.spicecompose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState

internal object DesktopBackDispatcher {
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

@Composable
actual fun SpiceBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentOnBack = rememberUpdatedState(onBack)

    DisposableEffect(enabled) {
        if (!enabled) {
            onDispose {}
        } else {
            val handler = { currentOnBack.value() }
            DesktopBackDispatcher.add(handler)
            onDispose { DesktopBackDispatcher.remove(handler) }
        }
    }
}
