package org.cf0x.spicecompose.ui.screen.controllers.control.hpm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun HpmController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val names = listOf("P1 Start","P1 1","P1 2","P1 3","P1 4","P2 Start","P2 1","P2 2","P2 3","P2 4")
    val widgets = remember { names.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }
    val si = if (subViewIndex % 2 == 1) 5 else 0
    val colors = listOf(Color(0xFFD05050), Color(0xFF5050E0), Color(0xFFF0F050), Color(0xFF50B050))

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(19f/9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp, idle: Color) {
                ControllerButton(widgets[wi], buttonControl, Modifier.offset(x, y).size(iw, ih).clip(CircleShape), idleColor = idle)
            }
            btn(si, w * 0.435f, h * 0.05f, w * 0.15f, w * 0.15f, ControllerColors.surface())
            for (i in 0..3) btn(si + 1 + i, w * (0.075f + 0.222f * i), h * 0.525f, w * 0.2f, w * 0.2f, colors[i])
        }
    }
}
