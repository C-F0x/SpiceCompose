package org.cf0x.spicecompose.ui.screen.controllers.control.rf3d

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
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
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun Rf3dController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val names = listOf("View","2D/3D","Wheel Left","Wheel Right","Accelerate","Brake","Auto Lever Down","Auto Lever Up")
    val widgets = remember { names.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }
    val icons = mapOf("View" to Icons.Rounded.Refresh, "2D/3D" to Icons.Rounded.Search,
        "Wheel Left" to Icons.Rounded.KeyboardArrowLeft, "Wheel Right" to Icons.Rounded.KeyboardArrowRight,
        "Accelerate" to Icons.Rounded.ArrowUpward, "Brake" to Icons.Rounded.ArrowDownward,
        "Auto Lever Down" to Icons.Rounded.ArrowDropDown, "Auto Lever Up" to Icons.Rounded.ArrowDropUp)

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(19f/9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                @Suppress("UNUSED_VARIABLE") val t = buttonControl.notifier.intValue
                Box(Modifier.offset(x, y).size(iw, ih)
                    .onGloballyPositioned { c -> buttonControl.updateBounds(widgets[wi].name, androidx.compose.ui.geometry.Rect(c.positionInWindow(), androidx.compose.ui.geometry.Size(c.size.width.toFloat(), c.size.height.toFloat()))) }
                    .background(if (widgets[wi].isDown) ControllerColors.buttonPressed() else ControllerColors.buttonIdle()),
                    contentAlignment = Alignment.Center) {
                    Icon(icons[widgets[wi].name] ?: Icons.Rounded.Refresh, null, tint = ControllerColors.onSurface())
                }
            }
            btn(0, w * 0.42f, h * 0.05f, w * 0.075f, w * 0.075f); btn(1, w * 0.51f, h * 0.05f, w * 0.075f, w * 0.075f)
            btn(2, w * 0.05f, h * 0.1f, w * 0.15f, h * 0.8f); btn(3, w * 0.22f, h * 0.1f, w * 0.15f, h * 0.8f)
            btn(4, w * 0.80f, h * 0.1f, w * 0.15f, h * 0.8f); btn(5, w * 0.63f, h * 0.1f, w * 0.15f, h * 0.8f)
            btn(6, w * 0.45f, h * 0.61f, w * 0.1f, h * 0.29f); btn(7, w * 0.45f, h * 0.29f, w * 0.1f, h * 0.3f)
        }
    }
}
