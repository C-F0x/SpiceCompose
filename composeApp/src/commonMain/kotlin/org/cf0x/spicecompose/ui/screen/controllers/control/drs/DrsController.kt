package org.cf0x.spicecompose.ui.screen.controllers.control.drs

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
fun DrsController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val names = listOf("P1 Start","P1 Up","P1 Down","P1 Left","P1 Right","P2 Start","P2 Up","P2 Down","P2 Left","P2 Right")
    val widgets = remember { names.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(19f/9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                ControllerButton(widgets[wi], buttonControl, Modifier.offset(x, y).size(iw, ih))
            }
            btn(0, w * 0.225f, h * 0.375f, w * 0.09f, w * 0.09f); btn(1, w * 0.165f, h * 0.070f, w * 0.12f, w * 0.12f); btn(2, w * 0.165f, h * 0.695f, w * 0.12f, w * 0.12f); btn(3, w * 0.015f, h * 0.370f, w * 0.12f, w * 0.12f); btn(4, w * 0.315f, h * 0.370f, w * 0.12f, w * 0.12f)
            btn(5, w * 0.76f, h * 0.375f, w * 0.09f, w * 0.09f); btn(6, w * 0.7f, h * 0.070f, w * 0.12f, w * 0.12f); btn(7, w * 0.7f, h * 0.695f, w * 0.12f, w * 0.12f); btn(8, w * 0.55f, h * 0.370f, w * 0.12f, w * 0.12f); btn(9, w * 0.85f, h * 0.370f, w * 0.12f, w * 0.12f)
        }
    }
}
