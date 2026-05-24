package org.cf0x.spicecompose.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Global design token accessor.
 * All corners and primary color are derived from a single source of truth here.
 * Usage: SpiceTheme.cornerShape(16.dp) / SpiceTheme.primary
 */
object SpiceTheme {

    /** Corner radius used across cards, sheets, dialogs. */
    val defaultCornerRadius: Dp = 16.dp
    val smallCornerRadius:   Dp = 8.dp
    val largeCornerRadius:   Dp = 24.dp

    /**
     * Returns the appropriate shape based on [LocalEnableSmoothCorner].
     * Smooth corners use a super-ellipse (squircle) approximation; fall back to
     * standard [RoundedCornerShape] on Material mode or when disabled.
     */
    @Composable
    fun cornerShape(radius: Dp = defaultCornerRadius): Shape {
        // SmoothRoundedCornerShape is Miuix-specific; use RoundedCornerShape universally
        // (Miuix components apply squircle internally; our custom surfaces use this)
        return RoundedCornerShape(radius)
    }

    /** The app primary color, inherited from the current MaterialTheme. */
    val primary: Color @Composable get() = MaterialTheme.colorScheme.primary

    val onPrimary: Color @Composable get() = MaterialTheme.colorScheme.onPrimary

    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface

    val onSurface: Color @Composable get() = MaterialTheme.colorScheme.onSurface
}
