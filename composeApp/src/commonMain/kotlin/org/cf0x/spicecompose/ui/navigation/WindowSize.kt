package org.cf0x.spicecompose.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSize {
    Compact,   // Phone portrait
    Medium,    // Tablet portrait / Foldable
    Expanded   // Tablet landscape / Desktop
}

fun getWindowSize(width: Dp): WindowSize = when {
    width < 600.dp -> WindowSize.Compact
    width < 840.dp -> WindowSize.Medium
    else -> WindowSize.Expanded
}

val LocalWindowSize = compositionLocalOf { WindowSize.Compact }
