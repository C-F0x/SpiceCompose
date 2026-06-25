package org.cf0x.spicecompose.ui.screen.controllers

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Theme-aware colors for game controllers. */
object ControllerColors {
    @Composable fun background() = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.background else MaterialTheme.colorScheme.background
    @Composable fun surface()   = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    @Composable fun onSurface() = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
    @Composable fun primary()   = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    /** Idle button fill — opaque enough to be visible on any background. */
    @Composable fun buttonIdle()    = surface()
    /** Pressed button fill — accent colour with high contrast against idle. */
    @Composable fun buttonPressed() = primary()
    /** Subtle border so idle buttons remain visible even on dark/black backgrounds. */
    @Composable fun buttonBorder()  = onSurface().copy(alpha = 0.12f)
}

/**
 * Shared button composable for game controllers.
 * Handles bounds reporting (window-coordinate) and theme-aware coloring.
 */
@Composable
fun ControllerButton(
    widget: ButtonControl.ButtonWidget,
    buttonControl: ButtonControl,
    modifier: Modifier = Modifier,
    idleColor: Color = ControllerColors.buttonIdle(),
    pressedColor: Color = ControllerColors.buttonPressed(),
) {
    @Suppress("UNUSED_VARIABLE")
    val tick = buttonControl.notifier.intValue
    Box(
        modifier
            .onGloballyPositioned { c ->
                buttonControl.updateBounds(
                    widget.name,
                    Rect(c.positionInWindow(), Size(c.size.width.toFloat(), c.size.height.toFloat()))
                )
            }
            .background(if (widget.isDown) pressedColor else idleColor)
            .then(if (!widget.isDown) Modifier.border(1.dp, ControllerColors.buttonBorder()) else Modifier)
    )
}
