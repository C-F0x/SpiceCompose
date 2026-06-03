package org.cf0x.spicecompose.platform

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

expect object SystemBarsManager {
    fun setFullscreen(enabled: Boolean)
}

val LocalFullscreenMode = compositionLocalOf { mutableStateOf(false) }
