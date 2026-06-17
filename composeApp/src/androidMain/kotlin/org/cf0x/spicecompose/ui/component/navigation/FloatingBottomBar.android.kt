package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.ui.component.miuix.animation.DampedDragAnimation
import org.cf0x.spicecompose.ui.component.miuix.animation.InteractiveHighlight
import org.cf0x.spicecompose.ui.component.miuix.liquid.*
import org.cf0x.spicecompose.ui.theme.isInDarkTheme
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.sensor.rememberDeviceTilt
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

private val iosIndicatorSpecular: Highlight = Highlight(
    width = 1.dp,
    alpha = 1f,
    style = BloomStroke(
        color = Color.White.copy(alpha = 0.12f),
        innerBlurRadius = 2.0.dp,
        primaryLight = LightSource(
            position = LightPosition(0.5f, -0.3f, -0.05f),
            color = Color.White,
            intensity = 1f,
        ),
        secondaryLight = LightSource(
            position = LightPosition(0.5f, 0.8f, -0.5f),
            color = Color.White,
            intensity = 0.4f,
        ),
        dualPeak = true,
    ),
)

@Composable
private fun rememberGravityRotatedHighlight(
    base: Highlight,
    extraDegrees: Float = 0f,
): Highlight {
    val baseStyle = base.style as BloomStroke
    val tiltState by rememberDeviceTilt()
    val tilt = tiltState
    
    val rotatedPrimary = remember(tilt, baseStyle.primaryLight, extraDegrees) {
        val basePrimary = baseStyle.primaryLight
        val gx = tilt.gravityX
        val gy = tilt.gravityY
        val gMagSq = gx * gx + gy * gy
        val (lx0, ly0) = if (gMagSq > 0.01f) {
            val invMag = 1f / sqrt(gMagSq)
            (gx * invMag) to (gy * invMag)
        } else {
            0f to -1f
        }
        val rad = extraDegrees * PI / 180.0
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()
        val lx = c * lx0 - s * ly0
        val ly = s * lx0 + c * ly0
        basePrimary.copy(
            position = LightPosition(
                x = 0.5f + lx,
                y = 0.7f + ly,
                z = basePrimary.position.z,
            ),
        )
    }
    return remember(base, rotatedPrimary) {
        base.copy(style = baseStyle.copy(primaryLight = rotatedPrimary))
    }
}

@Composable
actual fun FloatingBottomBar(
    modifier: Modifier,
    selectedIndex: () -> Int,
    onSelected: (index: Int) -> Unit,
    tabsCount: Int,
    isBlurEnabled: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    val isInDark = isInDarkTheme()
    val pillShape = remember { CircleShape }
    val accentColor = MiuixTheme.colorScheme.primary
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val containerColor = if (isBlurEnabled) surfaceContainer.copy(0.4f) else surfaceContainer

    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val tabsBackdrop = rememberLayerBackdrop()
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()

    var tabWidthPx by remember { mutableFloatStateOf(0f) }
    var totalWidthPx by remember { mutableFloatStateOf(0f) }

    val offsetAnimation = remember { Animatable(0f) }
    val rubberBandPx = with(density) { 4.dp.toPx() }
    val panelOffset by remember(rubberBandPx) {
        derivedStateOf {
            if (totalWidthPx == 0f) 0f
            else {
                val fraction = (offsetAnimation.value / totalWidthPx).fastCoerceIn(-1f, 1f)
                rubberBandPx * fraction.sign * EaseOut.transform(abs(fraction))
            }
        }
    }

    var currentIndex by remember(selectedIndex) { mutableIntStateOf(selectedIndex()) }

    class DampedDragAnimationHolder {
        var instance: DampedDragAnimation? = null
    }
    val holder = remember { DampedDragAnimationHolder() }

    val dampedDragAnimation = remember(animationScope, tabsCount, density, isLtr) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = selectedIndex().toFloat(),
            valueRange = 0f..(tabsCount - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 78f / 56f,
            canDrag = { offset ->
                val anim = holder.instance ?: return@DampedDragAnimation true
                if (tabWidthPx == 0f) return@DampedDragAnimation false
                val indicatorX = anim.value * tabWidthPx
                val padding = with(density) { 4.dp.toPx() }
                val globalTouchX = if (isLtr) padding + indicatorX + offset.x else totalWidthPx - padding - tabWidthPx - indicatorX + offset.x
                globalTouchX in 0f..totalWidthPx
            },
            onDragStarted = {},
            onDragStopped = {
                val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                currentIndex = targetIndex
                animateToValue(targetIndex.toFloat())
                animationScope.launch { offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f)) }
            },
            onDrag = { _, dragAmount ->
                if (tabWidthPx > 0) {
                    updateValue((targetValue + dragAmount.x / tabWidthPx * if (isLtr) 1f else -1f).fastCoerceIn(0f, (tabsCount - 1).toFloat()))
                    animationScope.launch { offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x) }
                }
            }
        ).also { holder.instance = it }
    }

    LaunchedEffect(selectedIndex) {
        snapshotFlow { selectedIndex() }.collectLatest { currentIndex = it }
    }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { currentIndex }.drop(1).collectLatest { index ->
            dampedDragAnimation.animateToValue(index.toFloat())
            onSelected(index)
        }
    }

    val interactiveHighlight = remember(animationScope, tabWidthPx) {
        InteractiveHighlight(
            animationScope = animationScope,
            position = { size, _ ->
                Offset(
                    if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidthPx + panelOffset
                    else size.width - (dampedDragAnimation.value + 0.5f) * tabWidthPx + panelOffset,
                    size.height / 2f
                )
            }
        )
    }

    val baseHighlight = rememberGravityRotatedHighlight(iosIndicatorSpecular, extraDegrees = -45f)
    val pillHighlight = rememberGravityRotatedHighlight(iosIndicatorSpecular, extraDegrees = 90f)
    val combinedBackdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop)

    Box(
        modifier = modifier.width(IntrinsicSize.Min),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            Modifier
                .onGloballyPositioned { coords ->
                    totalWidthPx = coords.size.width.toFloat()
                    val contentWidthPx = totalWidthPx - with(density) { 8.dp.toPx() }
                    tabWidthPx = (contentWidthPx / tabsCount).coerceAtLeast(0f)
                }
                .graphicsLayer { translationX = panelOffset }
                .dropShadow(
                    shape = pillShape,
                    shadow = Shadow(radius = 10.dp, color = Color.Black, alpha = if (isInDark) 0.2f else 0.1f),
                )
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .then(
                    if (isBlurEnabled) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { pillShape },
                            effects = {
                                vibrancy()
                                blur(4.dp.toPx(), 4.dp.toPx())
                                lens(refractionHeight = 24.dp.toPx(), refractionAmount = 24.dp.toPx())
                            },
                            highlight = { baseHighlight.copy(alpha = 0.75f) },
                            layerBlock = {
                                val w = size.width.coerceAtLeast(1f)
                                val s = lerp(1f, 1f + with(density) { 16.dp.toPx() } / w, dampedDragAnimation.pressProgress)
                                scaleX = s
                                scaleY = s
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier.background(containerColor, pillShape)
                    }
                )
                .then(if (isBlurEnabled) interactiveHighlight.modifier else Modifier)
                .height(64.dp)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )

        if (isBlurEnabled) {
            CompositionLocalProvider(
                LocalFloatingBottomBarTabScale provides {
                    lerp(1f, 1.2f, dampedDragAnimation.pressProgress)
                }
            ) {
                Row(
                    Modifier
                        .clearAndSetSemantics {}
                        .alpha(0f)
                        .layerBackdrop(tabsBackdrop)
                        .graphicsLayer { translationX = panelOffset }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { pillShape },
                            effects = {
                                vibrancy()
                                blur(4.dp.toPx(), 4.dp.toPx())
                                lens(refractionHeight = 24.dp.toPx(), refractionAmount = 24.dp.toPx())
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                        .then(interactiveHighlight.modifier)
                        .height(56.dp)
                        .padding(horizontal = 4.dp)
                        .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }

        if (tabWidthPx > 0f) {
            val tabWidthDp = with(density) { tabWidthPx.toDp() }
            if (isBlurEnabled) {
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) progressOffset + panelOffset else -progressOffset + panelOffset
                        }
                        .then(interactiveHighlight.gestureModifier)
                        .then(dampedDragAnimation.modifier)
                        .drawBackdrop(
                            backdrop = combinedBackdrop,
                            shape = { pillShape },
                            effects = {
                                val progress = dampedDragAnimation.pressProgress
                                lens(refractionHeight = 10.dp.toPx() * progress, refractionAmount = 14.dp.toPx() * progress, depthEffect = true, chromaticAberration = 0.5f)
                            },
                            highlight = { pillHighlight.copy(alpha = dampedDragAnimation.pressProgress) },
                            layerBlock = {
                                scaleX = dampedDragAnimation.scaleX
                                scaleY = dampedDragAnimation.scaleY
                                val velocity = dampedDragAnimation.velocity / 10f
                                scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
                                val progress = dampedDragAnimation.pressProgress
                                drawRect(color = if (!isInDark) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.1f), alpha = 1f - progress)
                                drawRect(Color.Black.copy(alpha = 0.03f * progress))
                            },
                        )
                        .innerShadow(shape = pillShape) {
                            InnerShadow(radius = 8.dp * dampedDragAnimation.pressProgress, color = Color.Black.copy(alpha = 0.15f), alpha = dampedDragAnimation.pressProgress)
                        }
                        .height(56.dp)
                        .width(tabWidthDp)
                )
            } else {
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) progressOffset + panelOffset else -progressOffset + panelOffset
                        }
                        .then(dampedDragAnimation.modifier)
                        .clip(pillShape)
                        .background(accentColor.copy(alpha = 0.15f), pillShape)
                        .height(56.dp)
                        .width(tabWidthDp)
                )
            }
        }
    }
}
