package org.cf0x.spicecompose.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun GameOptimizationEffect() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) GameOptimizations.enable(window)
        onDispose { GameOptimizations.disable() }
    }
}
