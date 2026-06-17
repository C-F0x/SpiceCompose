package org.cf0x.spicecompose.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private enum class DragZone { NONE, RING, SQUARE }

@Composable
fun ColorPickerWheel(
    initialColor: Color = Color(0xFF6750A4),
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert initial color to HSV
    val initHsv = remember(initialColor) { colorToHsv(initialColor) }

    var hue by remember { mutableFloatStateOf(initHsv[0]) }
    var sat by remember { mutableFloatStateOf(initHsv[1]) }
    var bri by remember { mutableFloatStateOf(initHsv[2]) }

    val currentColor by remember(hue, sat, bri) {
        derivedStateOf { Color.hsv(hue, sat, bri) }
    }

    // Precompute hue ring colors (360 steps)
    val hueColors = remember {
        List(361) { i -> Color.hsv(i.toFloat(), 1f, 1f) }
    }

    var hexInput     by remember { mutableStateOf("") }
    var isEditingHex by remember { mutableStateOf(false) }
    var hexError     by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(currentColor) {
        onColorChanged(currentColor)
        if (!isEditingHex) {
            hexInput = "#%06X".format((currentColor.value shr 32).toInt() and 0xFFFFFF)
        }
    }

    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val density    = LocalDensity.current
            val sizePx     = with(density) { maxWidth.toPx() }
            val ringWidth  = sizePx * 0.11f
            val outerR     = sizePx / 2f
            val innerR     = outerR - ringWidth
            val squareSide = innerR * sqrt(2f) * 0.9f
            val sqLeft     = sizePx / 2f - squareSide / 2f
            val sqTop      = sizePx / 2f - squareSide / 2f

            fun inRing(p: Offset): Boolean {
                val d = sqrt((p.x - sizePx / 2f).pow(2) + (p.y - sizePx / 2f).pow(2))
                return d in (innerR * 0.75f)..outerR
            }
            fun inSquare(p: Offset) =
                p.x in sqLeft..(sqLeft + squareSide) &&
                        p.y in sqTop..(sqTop + squareSide)

            fun handleRing(p: Offset) {
                hue = (atan2(p.y - sizePx / 2f, p.x - sizePx / 2f)
                        * (180f / PI.toFloat()) + 360f) % 360f
            }
            fun handleSquareClamped(p: Offset) {
                val cx = p.x.coerceIn(sqLeft, sqLeft + squareSide)
                val cy = p.y.coerceIn(sqTop,  sqTop  + squareSide)
                sat = ((cx - sqLeft) / squareSide).coerceIn(0f, 1f)
                bri = (1f - (cy - sqTop) / squareSide).coerceIn(0f, 1f)
            }

            var dragZone by remember { mutableStateOf(DragZone.NONE) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(squareSide, sqLeft, sqTop) {
                        detectDragGestures(
                            onDragStart = { p ->
                                dragZone = when {
                                    inRing(p)   -> DragZone.RING
                                    inSquare(p) -> DragZone.SQUARE
                                    else        -> DragZone.NONE
                                }
                            },
                            onDragEnd    = { dragZone = DragZone.NONE },
                            onDragCancel = { dragZone = DragZone.NONE },
                            onDrag = { change, _ ->
                                when (dragZone) {
                                    DragZone.RING   -> handleRing(change.position)
                                    DragZone.SQUARE -> handleSquareClamped(change.position)
                                    DragZone.NONE   -> Unit
                                }
                            }
                        )
                    }
                    .pointerInput(squareSide, sqLeft, sqTop) {
                        detectTapGestures { p ->
                            when {
                                inRing(p)   -> handleRing(p)
                                inSquare(p) -> handleSquareClamped(p)
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val midR   = (outerR + innerR) / 2f

                // Hue ring — sweep gradient
                drawCircle(
                    brush = Brush.sweepGradient(hueColors, center),
                    radius = midR,
                    style = Stroke(width = ringWidth)
                )

                // Indicator on hue ring
                val hRad = Math.toRadians(hue.toDouble())
                val ix   = center.x + midR * cos(hRad).toFloat()
                val iy   = center.y + midR * sin(hRad).toFloat()
                drawCircle(Color.White, ringWidth * 0.48f, Offset(ix, iy), style = Stroke(2.5.dp.toPx()))
                drawCircle(Color.hsv(hue, 1f, 1f), ringWidth * 0.38f, Offset(ix, iy))

                // Saturation / Brightness square
                drawRect(
                    brush   = Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.hsv(hue, 1f, 1f)),
                        startX = sqLeft, endX = sqLeft + squareSide
                    ),
                    topLeft = Offset(sqLeft, sqTop),
                    size    = Size(squareSide, squareSide)
                )
                drawRect(
                    brush   = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = sqTop, endY = sqTop + squareSide
                    ),
                    topLeft = Offset(sqLeft, sqTop),
                    size    = Size(squareSide, squareSide)
                )

                // Indicator on square
                val svX = sqLeft + sat * squareSide
                val svY = sqTop  + (1f - bri) * squareSide
                drawCircle(Color.White,   9.dp.toPx(), Offset(svX, svY), style = Stroke(3.dp.toPx()))
                drawCircle(Color.Black,   9.dp.toPx(), Offset(svX, svY), style = Stroke(1.dp.toPx()))
                drawCircle(currentColor,  6.dp.toPx(), Offset(svX, svY))
            }
        }

        // Hex input row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(currentColor)
            )

            OutlinedTextField(
                value         = hexInput,
                onValueChange = { input ->
                    isEditingHex = true
                    hexError     = false
                    val clean    = input.filter {
                        it.isDigit() || it in 'A'..'F' || it in 'a'..'f' || it == '#'
                    }
                    hexInput = if (clean.startsWith("#")) clean.take(7)
                    else "#${clean.take(6)}"
                },
                label          = { Text("Hex") },
                placeholder    = { Text("#6750A4") },
                singleLine     = true,
                isError        = hexError,
                supportingText = if (hexError) {{ Text("Invalid hex, format: #RRGGBB") }} else null,
                textStyle      = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType   = KeyboardType.Ascii,
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction      = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val parsed = parseHexColor(hexInput)
                        if (parsed != null) {
                            val arr = colorToHsv(parsed)
                            hue      = arr[0]; sat = arr[1]; bri = arr[2]
                            hexError = false
                        } else {
                            hexError = true
                        }
                        isEditingHex = false
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Convert a Compose Color to HSV float array [hue(0..360), sat(0..1), val(0..1)]. */
private fun colorToHsv(c: Color): FloatArray {
    val r = c.red
    val g = c.green
    val b = c.blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val hue = when {
        delta == 0f -> 0f
        max == r    -> 60f * (((g - b) / delta) % 6f)
        max == g    -> 60f * (((b - r) / delta) + 2f)
        else        -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0) it + 360f else it }

    val sat = if (max == 0f) 0f else delta / max
    val bri = max

    return floatArrayOf(hue, sat, bri)
}

private fun parseHexColor(input: String): Color? {
    val clean = input.removePrefix("#").trim().uppercase()
    if (clean.length != 6) return null
    if (clean.any { it !in '0'..'9' && it !in 'A'..'F' }) return null
    return runCatching {
        Color(("FF$clean").toLong(16))
    }.getOrNull()
}
