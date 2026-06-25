package org.cf0x.spicecompose.ui.screen.controllers.control.ddr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ButtonControl
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

@Composable
fun DdrController(connectionManager: ConnectionManager, subViewIndex: Int) {
    val buttonControl = remember { ButtonControl(connectionManager) }
    val widgets = remember { ddrButtonNames.map { buttonControl.registerWidget(it) } }
    LaunchedEffect(Unit) { buttonControl.init() }; val viewNo = subViewIndex % 4

    Box(buttonControl.pointerInputModifier().fillMaxSize().background(ControllerColors.background()), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            fun Dp.clamp(min: Dp, max: Dp) = coerceIn(min, max)

            @Composable fun cell(wi: Int, gridPos: Int, col: Int, row: Int, cellSize: Dp) {
                val activePositions = setOf(2, 4, 5, 6, 8)
                if (gridPos !in activePositions) return
                val btnSize = cellSize // cells touch — no gap
                val x = (cellSize * (col.toFloat() - 1f)).clamp(0.dp, w - btnSize)
                val y = (cellSize * (row.toFloat() - 1f)).clamp(0.dp, h - btnSize)
                val wgt = widgets[wi]
                val icon = @Composable { when (gridPos) {
                    2 -> Icon(Icons.Rounded.KeyboardArrowUp, null, tint = ControllerColors.onSurface())
                    4 -> Icon(Icons.Rounded.KeyboardArrowLeft, null, tint = ControllerColors.onSurface())
                    6 -> Icon(Icons.Rounded.KeyboardArrowRight, null, tint = ControllerColors.onSurface())
                    8 -> Icon(Icons.Rounded.KeyboardArrowDown, null, tint = ControllerColors.onSurface())
                    else -> Text("START", color = ControllerColors.onSurface(), fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(2.dp))
                } }
                @Suppress("UNUSED_VARIABLE") val tick = buttonControl.notifier.intValue
                Box(Modifier.offset(x, y).size(btnSize)
                    .onGloballyPositioned { c -> buttonControl.updateBounds(wgt.name, Rect(c.positionInWindow(), Size(c.size.width.toFloat(), c.size.height.toFloat()))) }
                    .background(if (wgt.isDown) ControllerColors.buttonPressed() else ControllerColors.buttonIdle()), contentAlignment = Alignment.Center) { icon() }
            }

            // Build one 3×3 grid for one player side.
            @Composable fun spGrid(startIdx: Int, panelBase: Int, originX: Dp, originY: Dp, gridSize: Dp, showStart: Boolean, label: String = "") {
                val cellSize = gridSize / 3f
                Box(Modifier.offset(originX, originY).size(gridSize)) {
                    cell(panelBase + 1, 2, 2, 1, cellSize) // Up
                    cell(panelBase + 3, 4, 1, 2, cellSize) // Left
                    if (showStart) cell(startIdx, 5, 2, 2, cellSize) // Start
                    cell(panelBase + 4, 6, 3, 2, cellSize) // Right
                    cell(panelBase + 2, 8, 2, 3, cellSize) // Down
                    if (!showStart && label.isNotEmpty()) {
                        Text(label, color = ControllerColors.onSurface().copy(alpha = 0.25f),
                            fontSize = (cellSize / 2f).value.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.offset(cellSize, cellSize).size(cellSize))
                    }
                }
            }

            when (viewNo) {
                // View 0: P1 SP — Panel, no Start, "P1" in center
                0 -> {
                    val short = minOf(w, h); val gridSize = short
                    val ox = ((w - gridSize) / 2f).clamp(0.dp, w); val oy = ((h - gridSize) / 2f).clamp(0.dp, h)
                    spGrid(0, 0, ox, oy, gridSize, showStart = false, label = "P1")
                }
                // View 1: P2 SP — Panel, no Start, "P2" in center
                1 -> {
                    val short = minOf(w, h); val gridSize = short
                    val ox = ((w - gridSize) / 2f).clamp(0.dp, w); val oy = ((h - gridSize) / 2f).clamp(0.dp, h)
                    spGrid(9, 9, ox, oy, gridSize, showStart = false, label = "P2")
                }
                // DP / Menu: long edge / 2, then SP logic per half
                2, 3 -> {
                    val halfW = w / 2f; val short = minOf(halfW, h)
                    val gridSize = (short * 0.9f).clamp(0.dp, short)
                    val oy = ((h - gridSize) / 2f).clamp(0.dp, h)
                    val ox1 = ((halfW - gridSize) / 2f).clamp(0.dp, halfW)
                    val ox2 = (halfW + (halfW - gridSize) / 2f).clamp(halfW, w)
                    val isMenu = viewNo == 3
                    spGrid(0, if (isMenu) 4 else 0, ox1, oy, gridSize, showStart = isMenu)
                    spGrid(9, if (isMenu) 13 else 9, ox2, oy, gridSize, showStart = isMenu)
                }
            }
        }
    }
}
