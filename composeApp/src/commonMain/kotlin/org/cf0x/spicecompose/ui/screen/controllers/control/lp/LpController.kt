package org.cf0x.spicecompose.ui.screen.controllers.control.lp

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
import androidx.compose.ui.unit.Dp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun LpController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val widgets = remember { listOf("Left", "Right").map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(19f/9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                ControllerButton(widgets[wi], buttonControl, Modifier.offset(x, y).size(iw, ih))
            }
            btn(0, w * 0.075f, h * 0.08f, w * 0.4f, w * 0.4f)
            btn(1, w * 0.535f, h * 0.08f, w * 0.4f, w * 0.4f)
        }
    }
}
