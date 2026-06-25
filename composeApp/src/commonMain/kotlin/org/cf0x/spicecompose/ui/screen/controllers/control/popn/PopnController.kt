package org.cf0x.spicecompose.ui.screen.controllers.control.popn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)
private val popnColors = listOf(Color(0xFFE0E0E0), Color(0xFFF0F050), Color(0xFF50B050), Color(0xFF5050E0), Color(0xFFD05050))

@Composable
fun PopnController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val widgets = remember { popnButtonNames.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }
    val viewNo = subViewIndex % 2; val round = viewNo == 0

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                val ci = if (wi == 8) 4 else wi % 5
                ControllerButton(widgets[wi], buttonControl,
                    Modifier.offset(x, y).size(iw, ih).then(if (round) Modifier.clip(CircleShape) else Modifier.clip(RoundedCornerShape(4.dp))),
                    idleColor = popnColors[ci], pressedColor = ControllerColors.buttonPressed())
            }
            for (i in 0..4) btn(i * 2, w * (0.075f + 0.175f * i), h * 0.525f, w * 0.15f, w * 0.2125f)
            for (i in 0..3) btn(i * 2 + 1, w * (0.1625f + 0.175f * i), h * 0.025f, w * 0.15f, w * 0.225f)
        }
    }
}
