package org.cf0x.spicecompose.ui.screen.controllers.control.ddr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun DdrController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val names = listOf("P1 Start","P1 Panel Up","P1 Panel Down","P1 Panel Left","P1 Panel Right","P1 Menu Up","P1 Menu Down","P1 Menu Left","P1 Menu Right","P2 Start","P2 Panel Up","P2 Panel Down","P2 Panel Left","P2 Panel Right","P2 Menu Up","P2 Menu Down","P2 Menu Left","P2 Menu Right")
    val widgets = remember { names.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }; val viewNo = subViewIndex % 4

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(if (viewNo <= 1) 19f/9f else 1f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp, label: String = "") {
                val wgt = widgets[wi]; val nm = wgt.name
                val icon = @Composable { when {
                    nm.contains("Panel Up") || nm.contains("Menu Up") -> Icon(Icons.Rounded.KeyboardArrowUp, null, tint = ControllerColors.onSurface())
                    nm.contains("Panel Down") || nm.contains("Menu Down") -> Icon(Icons.Rounded.KeyboardArrowDown, null, tint = ControllerColors.onSurface())
                    nm.contains("Panel Left") || nm.contains("Menu Left") -> Icon(Icons.Rounded.KeyboardArrowLeft, null, tint = ControllerColors.onSurface())
                    nm.contains("Panel Right") || nm.contains("Menu Right") -> Icon(Icons.Rounded.KeyboardArrowRight, null, tint = ControllerColors.onSurface())
                    else -> Text(label, color = ControllerColors.onSurface(), textAlign = TextAlign.Center) } }
                Box(Modifier.offset(x, y).size(iw, ih)
                    .onGloballyPositioned { c -> buttonControl.updateBounds(wgt.name, Rect(c.positionInWindow(), Size(c.size.width.toFloat(), c.size.height.toFloat()))) }
                    .background(if (wgt.isDown) ControllerColors.buttonPressed() else ControllerColors.buttonIdle()), contentAlignment = Alignment.Center) { icon() }
            }
            when (viewNo) {
                0 -> { btn(0, w * 0.205f, h * 0.375f, w * 0.09f, w * 0.09f, "P1"); btn(5, w * 0.19f, h * 0.07f, w * 0.12f, w * 0.12f); btn(6, w * 0.19f, h * 0.695f, w * 0.12f, w * 0.12f); btn(7, w * 0.10f, h * 0.37f, w * 0.12f, w * 0.12f); btn(8, w * 0.28f, h * 0.37f, w * 0.12f, w * 0.12f)
                    btn(9, w * 0.705f, h * 0.375f, w * 0.09f, w * 0.09f, "P2"); btn(14, w * 0.69f, h * 0.07f, w * 0.12f, w * 0.12f); btn(15, w * 0.69f, h * 0.695f, w * 0.12f, w * 0.12f); btn(16, w * 0.60f, h * 0.37f, w * 0.12f, w * 0.12f); btn(17, w * 0.78f, h * 0.37f, w * 0.12f, w * 0.12f) }
                1 -> for (p in 0..1) { val o = p * 9 + 1; val ox = if (p==0) w * 0.02f else w * 0.53f
                    btn(o, ox + w * 0.15f, w * 0.015f, w * 0.15f, w * 0.15f); btn(o+1, ox + w * 0.15f, w * 0.315f, w * 0.15f, w * 0.15f); btn(o+2, ox, w * 0.165f, w * 0.15f, w * 0.15f); btn(o+3, ox + w * 0.30f, w * 0.165f, w * 0.15f, w * 0.15f) }
                2 -> { btn(1, w * 0.35f, w * 0.05f, w * 0.3f, w * 0.3f); btn(2, w * 0.35f, w * 0.65f, w * 0.3f, w * 0.3f); btn(3, w * 0.05f, w * 0.35f, w * 0.3f, w * 0.3f); btn(4, w * 0.65f, w * 0.35f, w * 0.3f, w * 0.3f) }
                3 -> { btn(10, w * 0.35f, w * 0.05f, w * 0.3f, w * 0.3f); btn(11, w * 0.35f, w * 0.65f, w * 0.3f, w * 0.3f); btn(12, w * 0.05f, w * 0.35f, w * 0.3f, w * 0.3f); btn(13, w * 0.65f, w * 0.35f, w * 0.3f, w * 0.3f) }
            }
        }
    }
}
