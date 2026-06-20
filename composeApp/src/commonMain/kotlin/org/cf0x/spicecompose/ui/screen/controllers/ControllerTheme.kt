package org.cf0x.spicecompose.ui.screen.controllers

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
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Theme-aware colors for game controllers. */
object ControllerColors {
    @Composable fun background() = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.background else MaterialTheme.colorScheme.background
    @Composable fun surface()   = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    @Composable fun onSurface() = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
    @Composable fun primary()   = if (LocalUiMode.current == UiMode.Miuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    @Composable fun buttonIdle()    = surface().copy(alpha = 0.6f)
    @Composable fun buttonPressed() = primary()
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
    )
}
