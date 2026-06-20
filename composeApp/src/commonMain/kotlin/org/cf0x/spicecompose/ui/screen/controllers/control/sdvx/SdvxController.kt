package org.cf0x.spicecompose.ui.screen.controllers.control.sdvx

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

private val sdvxWidgetNames = listOf(
    "BT-A", "BT-B", "BT-C", "BT-D", "FX-L", "FX-R", "Start",
    "VOL-L Left", "VOL-L Right", "VOL-R Left", "VOL-R Right",
)

@Composable
fun SdvxController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val widgets = remember { sdvxWidgetNames.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(19f/9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                ControllerButton(widgets[wi], buttonControl, Modifier.offset(x, y).size(iw, ih))
            }
            for (i in 0..3) btn(i, w * (0.125f + 0.2f * i), h * 0.35f, w * 0.15f, w * 0.15f)
            btn(4, w * 0.2f, h * 0.75f, w * 0.2125f, w * 0.1f)
            btn(5, w * 0.575f, h * 0.75f, w * 0.2125f, w * 0.1f)
            btn(6, w * 0.45f, h * 0.05f, w * 0.1f, w * 0.1f)
            btn(7, w * 0.025f, h * 0.05f, w * 0.125f, w * 0.1f)
            btn(8, w * 0.175f, h * 0.05f, w * 0.125f, w * 0.1f)
            btn(9, w * 0.7f, h * 0.05f, w * 0.125f, w * 0.1f)
            btn(10, w * 0.85f, h * 0.05f, w * 0.125f, w * 0.1f)
        }
    }
}
