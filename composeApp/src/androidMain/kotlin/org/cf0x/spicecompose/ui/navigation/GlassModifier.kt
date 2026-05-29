package org.cf0x.spicecompose.ui.navigation

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

actual fun Modifier.liquidGlass(enabled: Boolean): Modifier {
    if (!enabled) return this
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            // True backdrop blur via RenderEffect (Android 12+)
            renderEffect = android.graphics.RenderEffect
                .createBlurEffect(40f, 40f, android.graphics.Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
            alpha = 0.88f
        }
    } else {
        // Pre-12 fallback: just reduce opacity for a translucent look
        this.graphicsLayer { alpha = 0.82f }
    }
}
