package org.cf0x.spicecompose.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
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

/**
 * Applies horizontal window insets (system bars + display cutout) to avoid
 * content being obscured by notches, punch-holes, or gesture bars in landscape.
 */
@Composable
fun Modifier.horizontalCutoutPadding(): Modifier = this.windowInsetsPadding(
    WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
)
