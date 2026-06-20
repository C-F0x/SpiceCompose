package org.cf0x.spicecompose.ui.screen.controllers.control.bbc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
private val bbcColors = listOf(Color(0xFFF05050), Color(0xFF50F050), Color(0xFF5050F0))

@Composable
fun BbcController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val names = (1..4).flatMap { p -> listOf("P$p R","P$p G","P$p B","P$p Disk-","P$p Disk+","P$p Disk -/+ Slowdown","P$p Disk -/+ Slowdown") }
    val widgets = remember { names.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }
    val si = subViewIndex % 4 * 7

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(19f/9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp, idle: Color = ControllerColors.buttonIdle()) {
                ControllerButton(widgets[wi], buttonControl, Modifier.offset(x, y).size(iw, ih), idleColor = idle)
            }
            for (i in 0..2) btn(si + i, w * (0.0625f + 0.3125f * i), h * 0.45f, w * 0.25f, h * 0.5f, bbcColors[i])
            btn(si + 3, w * 0.0625f, h * 0.05f, w * 0.3f, h * 0.3f, ControllerColors.surface())
            btn(si + 5, w * 0.2625f, h * 0.05f, w * 0.1f, h * 0.3f, ControllerColors.surface().copy(alpha = 0.5f))
            btn(si + 4, w * 0.6375f, h * 0.05f, w * 0.3f, h * 0.3f, ControllerColors.surface())
            btn(si + 6, w * 0.6375f, h * 0.05f, w * 0.1f, h * 0.3f, ControllerColors.surface().copy(alpha = 0.5f))
        }
    }
}
