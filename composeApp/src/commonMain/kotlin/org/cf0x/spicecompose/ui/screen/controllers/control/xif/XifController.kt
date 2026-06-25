package org.cf0x.spicecompose.ui.screen.controllers.control.xif

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.AnalogState
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsWrite
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun XifController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val widgets = remember { xifButtonNames.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }

    var faderL by remember { mutableFloatStateOf(0.5f) }
    var faderR by remember { mutableFloatStateOf(0.5f) }
    suspend fun send(n: String, v: Float) { connectionManager.getClient()?.analogsWrite(listOf(AnalogState(n, v.toDouble(), true))) }
    LaunchedEffect(faderL) { send("Fader-L", faderL) }
    LaunchedEffect(faderR) { send("Fader-R", faderR) }

    Column(Modifier.fillMaxSize().background(ControllerColors.background())) {
        Row(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            FaderBar("FADER-L", faderL, Modifier.weight(1f).fillMaxSize()) { faderL = it }
            FaderBar("FADER-R", faderR, Modifier.weight(1f).fillMaxSize()) { faderR = it }
        }
        Box(buttonControl.pointerInputModifier().fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
            BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                val w = maxWidth; val h = maxHeight
                @Composable fun btn(wi: Int, x: Dp, y: Dp, iw: Dp, ih: Dp) {
                    ControllerButton(widgets[wi], buttonControl, Modifier.offset(x, y).size(iw, ih).clip(RoundedCornerShape(4.dp)))
                }
                for (i in 0..11) btn(i, w * (i / 12f), h * 0.525f, w * (1f / 12f), w * 0.2125f)
            }
        }
    }
}

/**
 * Horizontal fader bar.  Press → follow finger (single-pointer mutex).
 * Release → instant snap to center 0.5.
 */
@Composable
private fun FaderBar(label: String, value: Float, modifier: Modifier, onValue: (Float) -> Unit) {
    var wPx by remember { mutableFloatStateOf(1f) }
    var activePointer by remember { mutableStateOf(-1L) }

    Box(
        modifier
            .pointerInput(label) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val ptrId = change.id.value

                        when (event.type) {
                            PointerEventType.Press -> {
                                if (activePointer < 0L) {
                                    activePointer = ptrId
                                    change.consume()
                                    onValue((change.position.x / size.width).coerceIn(0f, 1f))
                                }
                            }
                            PointerEventType.Move -> {
                                if (ptrId == activePointer) {
                                    change.consume()
                                    onValue((change.position.x / size.width).coerceIn(0f, 1f))
                                }
                            }
                            PointerEventType.Release -> {
                                if (ptrId == activePointer) {
                                    change.consume()
                                    activePointer = -1L
                                    onValue(0.5f)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
            .onGloballyPositioned { wPx = it.size.width.toFloat() },
        contentAlignment = Alignment.Center,
    ) {
        // Track background
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(ControllerColors.surface()))
        // Active fill
        Box(Modifier.fillMaxWidth(value).height(8.dp).clip(RoundedCornerShape(4.dp)).background(ControllerColors.primary()).align(Alignment.CenterStart))
        // Thumb — positioned in pixels to avoid Dp conversion mismatch
        if (wPx > 0f) {
            val density = LocalDensity.current.density
            Box(
                Modifier
                    .offset { IntOffset(((wPx * value - 12f * density).toInt()).coerceIn(0, (wPx - 24f * density).toInt()), 0) }
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
            )
        }
        // Label
        Text(label, color = ControllerColors.onSurface().copy(alpha = 0.5f), textAlign = TextAlign.Center, style = TextStyle(fontSize = 10.sp), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp))
    }
}
