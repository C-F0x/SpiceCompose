package org.cf0x.spicecompose.ui.screen.controllers.control.iidx

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun IidxController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val widgets = remember { iidxButtonNames.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }
    val viewNo = subViewIndex % 6

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            fun Dp.clamp(min: Dp, max: Dp) = coerceIn(min, max)
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                val sx = x.clamp(0.dp, w - iw.clamp(0.dp, w)); val sy = y.clamp(0.dp, h - ih.clamp(0.dp, h))
                ControllerButton(widgets[wi], buttonControl, Modifier.offset(sx, sy).size(iw, ih))
            }
            when (viewNo) {
                0 -> { val sf = 0.85f; val ttL = 0.0125f; val ttW = 0.115f
                    for (i in 0..2) btn(i, w * sf, h * (0.0625f + 0.125f * i), w * 0.10f, w * 0.10f)
                    for (i in 0..2) btn(3 + (i * 2 + 1), w * (0.225f + 0.175f * i), h * 0.025f, w * 0.15f, w * 0.225f)
                    for (i in 0..3) btn(3 + (i * 2), w * (0.15f + 0.175f * i), h * 0.525f, w * 0.15f, w * 0.2125f)
                    btn(10, w * ttL, h * 0.025f, w * ttW, h * 0.95f) }
                1 -> { btn(10, w * 0.05f, h * 0.05f, w * 0.9f, h * 0.9f)
                    Text("P1 TT +/-", color = ControllerColors.onSurface().copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.offset(w * 0.35f, h * 0.45f).size(w * 0.3f, h * 0.1f)) }
                2 -> { val sf = 0.025f; val ttL = 0.85f; val ttW = 0.13f
                    for (i in 0..2) btn(11 + i, w * sf, h * (0.0625f + 0.125f * i), w * 0.10f, w * 0.10f)
                    for (i in 0..2) btn(14 + (i * 2 + 1), w * (0.225f + 0.175f * i), h * 0.025f, w * 0.15f, w * 0.225f)
                    for (i in 0..3) btn(14 + (i * 2), w * (0.15f + 0.175f * i), h * 0.525f, w * 0.15f, w * 0.2125f)
                    btn(21, w * ttL, h * 0.025f, w * ttW, h * 0.95f) }
                3 -> { btn(21, w * 0.05f, h * 0.05f, w * 0.9f, h * 0.9f)
                    Text("P2 TT +/-", color = ControllerColors.onSurface().copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.offset(w * 0.35f, h * 0.45f).size(w * 0.3f, h * 0.1f)) }
                4 -> { val hw = w * 0.5f; val cx = hw - w * 0.04f; val bw = w * 0.08f
                    btn(0, cx, h * 0.15f, bw, bw); btn(1, cx, h * 0.40f, bw, bw); btn(2, cx, h * 0.65f, bw, bw)
                    for (i in 0..2) btn(3 + (i * 2 + 1), w * (0.225f + 0.175f * i) * 0.5f, h * 0.025f, hw * 0.15f, hw * 0.225f)
                    for (i in 0..3) btn(3 + (i * 2), w * (0.15f + 0.175f * i) * 0.5f, h * 0.525f, hw * 0.15f, hw * 0.2125f)
                    btn(10, hw * 0.0125f, h * 0.025f, hw * 0.115f, h * 0.95f)
                    for (i in 0..2) btn(14 + (i * 2 + 1), hw + w * (0.225f + 0.175f * i) * 0.5f, h * 0.025f, hw * 0.15f, hw * 0.225f)
                    for (i in 0..3) btn(14 + (i * 2), hw + w * (0.15f + 0.175f * i) * 0.5f, h * 0.525f, hw * 0.15f, hw * 0.2125f)
                    btn(21, hw + hw * 0.85f, h * 0.025f, hw * 0.13f, h * 0.95f) }
                5 -> { val hw = w * 0.5f
                    btn(10, hw * 0.025f, h * 0.05f, hw * 0.9f, h * 0.9f)
                    Text("P1 TT", color = ControllerColors.onSurface().copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.offset(hw * 0.20f, h * 0.45f).size(hw * 0.55f, h * 0.1f))
                    btn(21, hw + hw * 0.025f, h * 0.05f, hw * 0.9f, h * 0.9f)
                    Text("P2 TT", color = ControllerColors.onSurface().copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.offset(hw + hw * 0.20f, h * 0.45f).size(hw * 0.55f, h * 0.1f)) }
            }
        }
    }
}
