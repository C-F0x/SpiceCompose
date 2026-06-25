package org.cf0x.spicecompose.ui.screen.controllers.control.jb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun JbController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val widgets = remember { jbButtonNames.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(Color(0xFF102050)), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                @Suppress("UNUSED_VARIABLE") val t = buttonControl.notifier.intValue
                Box(Modifier.offset(x, y).size(iw, ih)
                    .onGloballyPositioned { c -> buttonControl.updateBounds(widgets[wi].name, Rect(c.positionInWindow(), Size(c.size.width.toFloat(), c.size.height.toFloat()))) }
                    .background(if (widgets[wi].isDown) ControllerColors.buttonPressed() else Color(0xFF101010))
                    .border(BorderStroke(1.dp, Color(0xFF102050))))
            }
            val pad = h * 0.04f; val gap = h * 0.02f
            val cellH = (h - pad * 2f - gap * 3f) * 0.25f; val cellW = (w - pad * 2f - gap * 3f) * 0.25f
            for (row in 0..3) for (col in 0..3) btn(row * 4 + col, pad + (cellW + gap) * col.toFloat(), pad + (cellH + gap) * row.toFloat(), cellW, cellH)
        }
    }
}
