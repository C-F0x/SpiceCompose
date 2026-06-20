package org.cf0x.spicecompose.ui.screen.controllers.control.we

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
import androidx.compose.ui.unit.Dp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun WeController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val names = listOf("Start","Up","Down","Left","Right","Button A","Button B","Button C","Button D","Button E","Button F")
    val widgets = remember { names.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(19f/9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                ControllerButton(widgets[wi], buttonControl, Modifier.offset(x, y).size(iw, ih).clip(CircleShape))
            }
            btn(0, w * 0.5f, h * 0.015f, w * 0.08f, w * 0.08f)
            btn(1, w * 0.12f, h * 0.065f, w * 0.15f, w * 0.15f); btn(2, w * 0.12f, h * 0.265f, w * 0.15f, w * 0.15f); btn(3, w * 0.02f, h * 0.165f, w * 0.15f, w * 0.15f); btn(4, w * 0.22f, h * 0.165f, w * 0.15f, w * 0.15f)
            for (i in 0..2) btn(5 + i, w * (0.5f + 0.15f * i), h * 0.165f, w * 0.125f, w * 0.125f)
            for (i in 0..2) btn(8 + i, w * (0.5f + 0.15f * i), h * 0.315f, w * 0.125f, w * 0.125f)
        }
    }
}
