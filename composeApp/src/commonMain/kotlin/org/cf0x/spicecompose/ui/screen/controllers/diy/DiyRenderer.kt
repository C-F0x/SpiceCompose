package org.cf0x.spicecompose.ui.screen.controllers.diy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.AnalogState
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsWrite
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerButton
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

/**
 * Renders a [DiyLayout] in Play mode.
 * Buttons go through ButtonControl; faders and knobs handle analog directly.
 */
@Composable
fun DiyRenderer(
    layout: DiyLayout,
    connectionManager: ConnectionManager,
) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val buttonWidgets = remember(layout) {
        val allBinds = mutableSetOf<String>()
        layout.widgets.filterIsInstance<DiyWidget.Button>().forEach { allBinds.add(it.bind) }
        layout.widgets.filterIsInstance<DiyWidget.Grid>().forEach { g -> g.cells.forEach { allBinds.add(it.bind) } }
        allBinds.filter { it.isNotEmpty() }.map { buttonControl.registerWidget(it) }
    }
    LaunchedEffect(layout) { buttonControl.init() }

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background())) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(16f / 9f), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight

            // ── Buttons ─────────────────────────────────────────────────
            layout.widgets.filterIsInstance<DiyWidget.Button>().filter { it.enabled }.forEachIndexed { idx, btn ->
                val bw = buttonWidgets.getOrNull(idx) ?: return@forEachIndexed
                val crPx = (btn.cornerRadius * minOf(w * btn.w, h * btn.h).value / 2f).dp
                val shape = RoundedCornerShape(crPx)
                val btnMod = Modifier.offset(w * btn.x - w * btn.w / 2f, h * btn.y - h * btn.h / 2f)
                    .size(w * btn.w, h * btn.h).graphicsLayer { rotationZ = btn.rotation }
                if (btn.sides != 4) {
                    // Invisible ControllerButton for touch tracking + polygon overlay
                    val polyColor = ControllerColors.buttonIdle()
                    ControllerButton(bw, buttonControl, btnMod.clip(shape).graphicsLayer { alpha = 0.99f })
                    Box(btnMod, contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val cx = size.width / 2f; val cy = size.height / 2f
                            val rad = minOf(size.width, size.height) / 2f * (1f - btn.cornerRadius * 0.5f)
                            val path = androidx.compose.ui.graphics.Path()
                            for (i in 0 until btn.sides) {
                                val a = (2.0 * kotlin.math.PI * i / btn.sides - kotlin.math.PI / 2.0)
                                val px = cx + rad * kotlin.math.cos(a).toFloat()
                                val py = cy + rad * kotlin.math.sin(a).toFloat()
                                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            path.close(); drawPath(path, polyColor)
                        }
                    }
                } else {
                    ControllerButton(bw, buttonControl, btnMod.clip(shape))
                }
            }

            // ── Grids ───────────────────────────────────────────────────
            layout.widgets.filterIsInstance<DiyWidget.Grid>().filter { it.enabled }.forEach { grid ->
                for (row in 0 until grid.rows) for (col in 0 until grid.cols) {
                    val cell = grid.cells.find { it.row == row && it.col == col }
                    val bind = cell?.bind ?: ""
                    if (bind.isEmpty()) continue
                    val bw = buttonWidgets.find { it.name == bind } ?: continue
                    val gx = w * grid.x + w * (grid.cellW + grid.gap) * col
                    val gy = h * grid.y + h * (grid.cellH + grid.gap) * row
                    ControllerButton(bw, buttonControl,
                        Modifier.offset(gx, gy).size(w * grid.cellW, h * grid.cellH)
                            .clip(RoundedCornerShape(grid.cornerRadius.dp)))
                }
            }

            // ── Faders ──────────────────────────────────────────────────
            layout.widgets.filterIsInstance<DiyWidget.Fader>().filter { it.enabled }.forEach { fader ->
                DiyFaderBar(fader, w, h, connectionManager)
            }

            // ── Knobs ───────────────────────────────────────────────────
            layout.widgets.filterIsInstance<DiyWidget.Knob>().filter { it.enabled }.forEach { knob ->
                DiyKnob(knob, w, h, connectionManager)
            }

            // ── Labels ──────────────────────────────────────────────────
            layout.widgets.filterIsInstance<DiyWidget.Label>().filter { it.enabled }.forEach { lbl ->
                Text(lbl.text, color = ControllerColors.onSurface(),
                    fontSize = lbl.fontSize.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.offset(w * lbl.x, h * lbl.y)
                        .graphicsLayer { rotationZ = lbl.rotation })
            }

            // ── Icons (Material names) ──────────────────────────────────
            layout.widgets.filterIsInstance<DiyWidget.Icon>().filter { it.enabled }.forEach { icn ->
                val img = DiyIconRegistry.get(icn.iconName)
                if (img != null) {
                    Icon(img, null, tint = ControllerColors.onSurface(),
                        modifier = Modifier.offset(w * icn.x - w * icn.size / 2f, h * icn.y - h * icn.size / 2f).size(w * icn.size)
                            .graphicsLayer { rotationZ = icn.rotation })
                }
            }
        }
    }
}

// ── Fader bar ───────────────────────────────────────────────────────────

@Composable
private fun DiyFaderBar(fader: DiyWidget.Fader, w: Dp, h: Dp, cm: ConnectionManager) {
    var value by remember { mutableFloatStateOf(0.5f) }
    var activePointer by remember { mutableStateOf(-1L) }

    Box(Modifier.offset(w * fader.x - w * fader.w / 2f, h * fader.y - h * fader.h / 2f)
        .size(w * fader.w, h * fader.h)
        .pointerInput(fader.id) {
            awaitPointerEventScope {
                while (true) {
                    val e = awaitPointerEvent(); val ch = e.changes.firstOrNull() ?: continue
                    when (e.type) {
                        PointerEventType.Press -> {
                            if (activePointer < 0L) { activePointer = ch.id.value; ch.consume()
                                value = (ch.position.x / size.width).coerceIn(0f, 1f) }
                        }
                        PointerEventType.Move -> {
                            if (ch.id.value == activePointer) { ch.consume()
                                value = (ch.position.x / size.width).coerceIn(0f, 1f) }
                        }
                        PointerEventType.Release -> {
                            if (ch.id.value == activePointer) { ch.consume(); activePointer = -1L
                                if (fader.autoReturn) value = 0.5f }
                        }
                        else -> {}
                    }
                }
            }
        }
        .clip(RoundedCornerShape(4.dp)).background(ControllerColors.surface()),
        contentAlignment = Alignment.CenterStart) {
        if (fader.style == "full") {
            // Full style: entire track filled proportionally
            val trackColor = if (fader.colorize) Color(0xFF4CAF50) else ControllerColors.primary()
            Box(Modifier.fillMaxSize(value).background(trackColor))
        } else {
            // Thin style: thin bar + thumb
            Box(Modifier.fillMaxWidth(value).height(h * fader.h * 0.6f).background(ControllerColors.primary()))
            Box(Modifier.offset(x = w * fader.w * value - 4.dp).width(8.dp).height(h * fader.h * 1.3f)
                .clip(RoundedCornerShape(4.dp)).background(ControllerColors.onSurface()))
        }
    }
    // Send analog
    LaunchedEffect(value) {
        cm.getClient()?.analogsWrite(listOf(AnalogState(fader.bind, value.toDouble(), true)))
    }
}

// ── Rotary knob ─────────────────────────────────────────────────────────

@Composable
private fun DiyKnob(knob: DiyWidget.Knob, w: Dp, h: Dp, cm: ConnectionManager) {
    val r = w * knob.radius
    var angle by remember { mutableFloatStateOf(0f) }  // radians
    var activePointer by remember { mutableStateOf(-1L) }
    var lastAngle by remember { mutableFloatStateOf(0f) }
    val value = ((angle / (2f * PI.toFloat()) * 100f + 50f) % 100f + 100f) % 100f

    Box(Modifier.offset(w * knob.x - r, h * knob.y - r).size(r * 2f)
        .pointerInput(knob.id) {
            awaitPointerEventScope {
                while (true) {
                    val e = awaitPointerEvent(); val ch = e.changes.firstOrNull() ?: continue
                    when (e.type) {
                        PointerEventType.Press -> if (activePointer < 0L) { activePointer = ch.id.value; ch.consume()
                            lastAngle = atan2(ch.position.y - size.height / 2f, ch.position.x - size.width / 2f) }
                        PointerEventType.Move -> if (ch.id.value == activePointer) { ch.consume()
                            val a = atan2(ch.position.y - size.height / 2f, ch.position.x - size.width / 2f)
                            var d = a - lastAngle; if (d > PI) d -= 2f * PI.toFloat() else if (d < -PI) d += 2f * PI.toFloat()
                            angle += d; lastAngle = a }
                        PointerEventType.Release -> if (ch.id.value == activePointer) { ch.consume(); activePointer = -1L
                            if (knob.autoReturn) angle = 0f }
                        else -> {}
                    }
                }
            }
        },
        contentAlignment = Alignment.Center) {
        // Outer ring
        Box(Modifier.size(r * 2f).clip(CircleShape).background(ControllerColors.surface()))
        // Inner circle
        Box(Modifier.size(r * 1.4f).clip(CircleShape).background(ControllerColors.background()))
        // Tick mark — rotates with angle
        if (knob.showTick) {
            Box(Modifier.size(r * 2f).graphicsLayer { rotationZ = angle * 180f / PI.toFloat() }, contentAlignment = Alignment.TopCenter) {
                Box(Modifier.size(2.dp, r * 0.3f).offset(y = r * 0.15f).background(ControllerColors.primary()))
            }
        }
    }
    LaunchedEffect(value) {
        cm.getClient()?.analogsWrite(listOf(AnalogState(knob.bind, (value / 100.0), true)))
    }
}
