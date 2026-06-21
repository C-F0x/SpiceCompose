package org.cf0x.spicecompose.ui.screen.controllers.diy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.roundToInt
import org.cf0x.spicecompose.ui.screen.controllers.ControllerColors

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

/**
 * Canvas editor for a [DiyLayout].
 *
 * v3 changes:
 *  - Removed popup bubble (→ sidebar inline property editor)
 *  - Removed bind mode (→ sidebar Tab2 drag-to-bind)
 *  - Added cross-component drag wire overlay + drop support
 *  - Canvas gestures (zoom/pan/drag-move) unchanged
 */
@Composable
fun DiyEditor(
    layout: DiyLayout,
    onWidgetMoved: (String, Float, Float) -> Unit,
    selectedId: String,
    dragWireBind: String?,
    dragWidgetType: String?,
    dragPtrX: Float,
    dragPtrY: Float,
    wireStartX: Float,
    wireStartY: Float,
    justBoundId: String?,
    canvasPanZoom: Boolean,
    canvasWidgetMove: Boolean,
    onDropBind: (bindName: String, widgetId: String) -> Unit,
    onDropWidget: (type: String, x: Float, y: Float) -> Unit,
    onCancelDrag: () -> Unit,
) {
    var dragTarget by remember { mutableStateOf<String?>(null) }
    var dragStartX by remember { mutableFloatStateOf(0f) }; var dragStartY by remember { mutableFloatStateOf(0f) }
    var widgetStartX by remember { mutableFloatStateOf(0f) }; var widgetStartY by remember { mutableFloatStateOf(0f) }
    var panning by remember { mutableStateOf(false) }
    var panStartX by remember { mutableFloatStateOf(0f) }; var panStartY by remember { mutableFloatStateOf(0f) }
    var panOrigX by remember { mutableFloatStateOf(0f) }; var panOrigY by remember { mutableFloatStateOf(0f) }

    var scale by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }; var panY by remember { mutableFloatStateOf(0f) }

    // Cross-drag pointer tracking
    // (dragPtrX / dragPtrY now provided by parent for global tracking)

    val grid = layout.grid
    val snapToGrid = grid.enabled && grid.snapToLine

    BoxWithConstraints(
        Modifier.fillMaxSize().background(ControllerColors.background())
            .then(if (canvasPanZoom) Modifier.pointerInput("zoom") { detectTransformGestures { c, _, z, _ ->
                val oldScale = scale; scale = (scale * z).coerceIn(0.5f, 3f)
                panX = c.x + (panX - c.x) * (scale / oldScale); panY = c.y + (panY - c.y) * (scale / oldScale)
            }} else Modifier)
            .pointerInput("drag", canvasPanZoom, canvasWidgetMove) {
                awaitPointerEventScope {
                    while (true) {
                        val e = awaitPointerEvent(); val ch = e.changes.firstOrNull() ?: continue
                        val fracX = (ch.position.x / size.width).coerceIn(0f, 1f)
                        val fracY = (ch.position.y / size.height).coerceIn(0f, 1f)

                        when (e.type) {
                            PointerEventType.Press -> {
                                // Library drag drop on press
                                if (dragWidgetType != null) {
                                    onDropWidget(dragWidgetType, fracX, fracY)
                                    return@awaitPointerEventScope
                                }
                                // Wire drop on press
                                if (dragWireBind != null) {
                                    val hit = hitTest(layout.widgets, fracX, fracY)
                                    if (hit != null) onDropBind(dragWireBind, hit.id)
                                    else onCancelDrag()
                                    return@awaitPointerEventScope
                                }
                                // Widget center snaps to finger immediately (first pointer only)
                                val hit = hitTest(layout.widgets, fracX, fracY)
                                if (canvasWidgetMove && hit != null && hit.id == selectedId) {
                                    onWidgetMoved(selectedId, fracX, fracY)
                                    dragTarget = hit.id; dragStartX = fracX; dragStartY = fracY
                                    val wf = widgetFraction(hit); widgetStartX = wf.x; widgetStartY = wf.y
                                } else if (canvasPanZoom) {
                                    panning = true; panStartX = fracX; panStartY = fracY
                                    panOrigX = panX; panOrigY = panY
                                }
                            }
                            PointerEventType.Move -> {
                                dragTarget?.let { id ->
                                    var nx = fracX; var ny = fracY
                                    if (snapToGrid) { val sx=grid.xStep/100f; val sy=grid.yStep/100f; nx=(nx/sx).roundToInt()*sx; ny=(ny/sy).roundToInt()*sy }
                                    onWidgetMoved(id, nx.coerceIn(0f,1f), ny.coerceIn(0f,1f))
                                }
                                if (panning) { panX = panOrigX + (fracX - panStartX) * size.width; panY = panOrigY + (fracY - panStartY) * size.height }
                            }
                            PointerEventType.Release -> {
                                // Wire/Widget drop: bind or create on release
                                if (dragWireBind != null) {
                                    val hit = hitTest(layout.widgets, fracX, fracY)
                                    if (hit != null) onDropBind(dragWireBind, hit.id)
                                    else onCancelDrag()
                                    return@awaitPointerEventScope
                                }
                                if (dragWidgetType != null) {
                                    onDropWidget(dragWidgetType, fracX, fracY)
                                    return@awaitPointerEventScope
                                }
                                dragTarget = null; panning = false
                            }
                            else -> {}
                        }
                    }
                }
            }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(16f/9f).graphicsLayer { scaleX = scale; scaleY = scale; translationX = panX; translationY = panY; transformOrigin = TransformOrigin(0f,0f) }, contentAlignment = Alignment.TopStart) {
            val w = maxWidth; val h = maxHeight
            val surfaceColor = ControllerColors.surface(); val primaryColor = ControllerColors.primary()

            // Grid — controlled by GuideGridIndicator widget
            val guideGridEnabled = layout.widgets.any { it is DiyWidget.GuideGridIndicator && it.enabled }
            if (grid.enabled || guideGridEnabled) Canvas(Modifier.fillMaxSize()) { val lc = surfaceColor
                for (i in 0..100 step grid.xStep) drawLine(lc, Offset(size.width*i/100f,0f), Offset(size.width*i/100f,size.height), strokeWidth=0.5f, pathEffect=PathEffect.dashPathEffect(floatArrayOf(4f,4f)))
                for (i in 0..100 step grid.yStep) drawLine(lc, Offset(0f,size.height*i/100f), Offset(size.width,size.height*i/100f), strokeWidth=0.5f, pathEffect=PathEffect.dashPathEffect(floatArrayOf(4f,4f))) }

            // Guide points / lines (first-class widgets)
            layout.widgets.filterIsInstance<DiyWidget.GuidePointWidget>().forEach { pt ->
                Box(Modifier.offset(w*pt.x-4.dp, h*pt.y-4.dp).size(8.dp).clip(CircleShape).background(primaryColor))
            }
            layout.widgets.filterIsInstance<DiyWidget.GuideLineWidget>().forEach { gl ->
                Canvas(Modifier.fillMaxSize()) { val c = primaryColor
                    if (gl.orient=="h") drawLine(c, Offset(0f,size.height*gl.pos), Offset(size.width,size.height*gl.pos), strokeWidth=1.5f)
                    else drawLine(c, Offset(size.width*gl.pos,0f), Offset(size.width*gl.pos,size.height), strokeWidth=1.5f) }
            }

            // Widgets (rendered in list order = priority; later = on top)
            layout.widgets.forEach { widget ->
                val f = widgetFraction(widget); val x = w*f.x - w*f.w/2f; val y = h*f.y - h*f.h/2f; val iw = w*f.w; val ih = h*f.h
                val sel = widget.id == selectedId
                val bound = widget.id == justBoundId
                val enabled = widget.enabled
                val selOverlay = when {
                    bound -> Modifier.background(Color(0xFF4CAF50).copy(alpha = 0.35f))
                    sel -> Modifier.background(ControllerColors.primary().copy(alpha = 0.25f))
                    else -> Modifier
                }
                val selBorder = if (sel) Modifier.border(2.dp, ControllerColors.primary().copy(alpha = 0.6f)) else Modifier
                val alphaMod = if (!enabled) Modifier.graphicsLayer { alpha = 0.3f } else Modifier
                val rot = when (widget) { is DiyWidget.Button -> widget.rotation; is DiyWidget.Label -> widget.rotation; is DiyWidget.Icon -> widget.rotation; else -> 0f }
                val diag = maxOf(iw, ih) * 1.42f

                when (widget) {
                    is DiyWidget.Button -> {
                        val crPx = (widget.cornerRadius * minOf(iw, ih).value / 2f).dp
                        val buttonMod = Modifier.offset(x - (diag-iw)/2f, y - (diag-ih)/2f).size(diag).graphicsLayer { rotationZ = rot }.then(alphaMod)
                        if (widget.sides == 4) {
                            Box(buttonMod, contentAlignment = Alignment.Center) {
                                Box(Modifier.size(iw, ih).clip(RoundedCornerShape(crPx)).background(ControllerColors.buttonIdle()).then(selOverlay).then(selBorder), contentAlignment = Alignment.Center) {
                                    Text(widget.id, fontSize=8.sp, color=ControllerColors.onSurface(), textAlign=TextAlign.Center) }
                            }
                        } else {
                            // Polygon
                            val polyFill = ControllerColors.buttonIdle()
                            val polySel = ControllerColors.primary().copy(alpha = 0.25f)
                            val polyBound = Color(0xFF4CAF50).copy(alpha = 0.35f)
                            Canvas(buttonMod) {
                                val cx = size.width / 2f; val cy = size.height / 2f
                                val rad = minOf(size.width, size.height) / 2f * (1f - widget.cornerRadius * 0.5f)
                                val path = androidx.compose.ui.graphics.Path()
                                for (i in 0 until widget.sides) {
                                    val a = (2.0 * kotlin.math.PI * i / widget.sides - kotlin.math.PI / 2.0)
                                    val px = cx + rad * kotlin.math.cos(a).toFloat()
                                    val py = cy + rad * kotlin.math.sin(a).toFloat()
                                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                }
                                path.close()
                                drawPath(path, polyFill)
                                if (sel) drawPath(path, polySel)
                                if (bound) drawPath(path, polyBound)
                            }
                        }
                    }
                    is DiyWidget.Fader -> {
                        Box(Modifier.offset(x,y).size(iw,ih).then(selOverlay).then(selBorder).then(alphaMod).clip(RoundedCornerShape(4.dp)).background(ControllerColors.surface()), contentAlignment = Alignment.CenterStart) {
                            Box(Modifier.fillMaxWidth(0.5f).height(ih).background(ControllerColors.primary()))
                            Box(Modifier.offset(x = iw * 0.5f - 4.dp).width(8.dp).height(ih * 1.3f).clip(RoundedCornerShape(4.dp)).background(ControllerColors.onSurface())) }
                        Text(widget.id, fontSize=8.sp, color=ControllerColors.onSurface(), textAlign=TextAlign.Center)
                    }
                    is DiyWidget.Knob -> Box(Modifier.offset(x-iw/2f,y-ih/2f).size(iw).then(selOverlay).then(selBorder).then(alphaMod).clip(CircleShape).background(ControllerColors.surface()), contentAlignment=Alignment.Center) {
                        Box(Modifier.size(iw*0.7f).clip(CircleShape).background(ControllerColors.background())); Text(widget.id, fontSize=8.sp, color=ControllerColors.onSurface(), textAlign=TextAlign.Center) }
                    is DiyWidget.Label -> Box(Modifier.offset(x-(diag-iw)/2f, y-(diag-ih)/2f).size(diag).graphicsLayer { rotationZ = rot }.then(alphaMod), contentAlignment = Alignment.Center) {
                        Text(widget.text, color=if(sel) ControllerColors.primary() else ControllerColors.onSurface(), fontSize=widget.fontSize.sp) }
                    is DiyWidget.Icon -> { val img = DiyIconRegistry.get(widget.iconName); if(img!=null) Box(Modifier.offset(x-(diag-iw)/2f, y-(diag-ih)/2f).size(diag).graphicsLayer { rotationZ = rot }.then(alphaMod), contentAlignment = Alignment.Center) {
                        Icon(img, null, modifier=Modifier.size(iw), tint=if(sel) ControllerColors.primary() else ControllerColors.onSurface()) } }
                    is DiyWidget.Grid -> for (row in 0 until widget.rows) for (col in 0 until widget.cols) {
                        val gx = w*widget.x + w*(widget.cellW+widget.gap)*col; val gy = h*widget.y + h*(widget.cellH+widget.gap)*row
                        val cell = widget.cells.find { it.row==row && it.col==col }; val isBound = cell?.bind?.isNotEmpty()==true
                        Box(Modifier.offset(gx,gy).size(w*widget.cellW, h*widget.cellH).then(selOverlay).then(selBorder).then(alphaMod).clip(RoundedCornerShape(widget.cornerRadius.dp)).background(if(isBound) ControllerColors.buttonPressed().copy(alpha=0.3f) else ControllerColors.buttonIdle()), contentAlignment=Alignment.Center) {
                            Text(if(isBound) cell!!.bind.takeLast(4) else "$row,$col", fontSize=7.sp, color=ControllerColors.onSurface(), textAlign=TextAlign.Center) }
                    }
                    is DiyWidget.GuideLineWidget -> {} // rendered above
                    is DiyWidget.GuidePointWidget -> {} // rendered above
                    is DiyWidget.GuideGridIndicator -> {} // controls grid visibility
                }
            }

            // Canvas border — drawn on the 16:9 area only
            Canvas(Modifier.fillMaxSize()) {
                drawRect(primaryColor.copy(alpha = 0.4f), style = Stroke(1.5.dp.toPx()))
            }
        }

        // ── Cross-drag wire overlay ────────────────────────────────────
        if (dragWireBind != null) {
            val density = LocalDensity.current
            val outerW = maxWidth; val outerH = maxHeight
            val canvasW: Dp; val canvasH: Dp
            if (outerW / outerH > 16f / 9f) { canvasH = outerH; canvasW = outerH * 16f / 9f }
            else { canvasW = outerW; canvasH = outerW * 9f / 16f }
            val offsetX = (outerW - canvasW) / 2f
            val offsetY = (outerH - canvasH) / 2f

            val canvasPX = (dragPtrX - with(density) { offsetX.toPx() }).coerceIn(0f, with(density) { canvasW.toPx() })
            val canvasPY = (dragPtrY - with(density) { offsetY.toPx() }).coerceIn(0f, with(density) { canvasH.toPx() })
            val fracX = (canvasPX / with(density) { canvasW.toPx() }).coerceIn(0f, 1f)
            val fracY = (canvasPY / with(density) { canvasH.toPx() }).coerceIn(0f, 1f)
            val hovered = hitTest(layout.widgets, fracX, fracY)

            if (hovered != null) {
                val hf = widgetFraction(hovered)
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(Color(0xFF00E5FF).copy(alpha = 0.4f),
                        topLeft = Offset(canvasW.toPx() * hf.x - canvasW.toPx() * hf.w / 2f + offsetX.toPx(),
                                         canvasH.toPx() * hf.y - canvasH.toPx() * hf.h / 2f + offsetY.toPx()),
                        size = androidx.compose.ui.geometry.Size(canvasW.toPx() * hf.w, canvasH.toPx() * hf.h),
                        style = Stroke(3.dp.toPx()))
                }
            }
            // Solid wire from start position to pointer
            Canvas(Modifier.fillMaxSize()) {
                val start = Offset(wireStartX, wireStartY)
                val end = Offset(dragPtrX, dragPtrY)
                drawLine(Color.Cyan.copy(alpha = 0.3f), start, end, strokeWidth = 8f)
                drawLine(Color.Cyan, start, end, strokeWidth = 3f)
                drawCircle(Color.Cyan, 7f, center = end)
                drawCircle(Color.White, 3f, center = end)
                drawCircle(Color.Cyan.copy(alpha = 0.5f), 5f, center = start)
            }
        }

        // Ghost for library drag
        if (dragWidgetType != null) {
            val ghostLabel = when (dragWidgetType) {
                "button" -> "⬜ Button"
                "fader" -> "▬ Fader"
                "knob" -> "◯ Knob"
                "label" -> "T Label"
                "icon" -> "✦ Icon"
                "grid" -> "⊞ Grid"
                else -> dragWidgetType!!
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Text(ghostLabel, color = Color.Cyan.copy(alpha = 0.7f), fontSize = 14.sp,
                    modifier = Modifier.offset(dragPtrX.toInt().dp, dragPtrY.toInt().dp - 16.dp))
            }
        }
    }
}

// ── Hit-test helper ─────────────────────────────────────────────────────

private fun hitTest(widgets: List<DiyWidget>, fracX: Float, fracY: Float): DiyWidget? {
    return widgets.reversed().firstOrNull { w ->
        val wf = widgetFraction(w)
        fracX in (wf.x-wf.w/2f)..(wf.x+wf.w/2f) && fracY in (wf.y-wf.h/2f)..(wf.y+wf.h/2f)
    }
}

// ── Fraction helper ─────────────────────────────────────────────────────

private fun widgetFraction(w: DiyWidget): DiyFraction = when (w) {
    is DiyWidget.Button -> DiyFraction(w.x,w.y,w.w,w.h)
    is DiyWidget.Fader -> DiyFraction(w.x,w.y,w.w,w.h)
    is DiyWidget.Knob -> DiyFraction(w.x,w.y,w.radius*2f,w.radius*2f)
    is DiyWidget.Label -> DiyFraction(w.x,w.y,0.1f,0.04f)
    is DiyWidget.Icon -> DiyFraction(w.x,w.y,w.size,w.size)
    is DiyWidget.Grid -> DiyFraction(w.x,w.y,w.cols*(w.cellW+w.gap)-w.gap,w.rows*(w.cellH+w.gap)-w.gap)
    is DiyWidget.GuideLineWidget -> if (w.orient == "h") DiyFraction(0.5f, w.pos, 1f, 0.005f) else DiyFraction(w.pos, 0.5f, 0.005f, 1f)
    is DiyWidget.GuidePointWidget -> DiyFraction(w.x, w.y, 0.01f, 0.01f)
    is DiyWidget.GuideGridIndicator -> DiyFraction(0.5f, 0.5f, 0f, 0f)
}

private data class DiyFraction(val x: Float, val y: Float, val w: Float, val h: Float)
