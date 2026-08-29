package org.cf0x.spicecompose.ui.navigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

actual fun Modifier.liquidGlass(enabled: Boolean): Modifier =
    if (enabled) this.graphicsLayer { alpha = 0.85f } else this
