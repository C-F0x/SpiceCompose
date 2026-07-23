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
 *
 * Material 3 mode → delegates to [MaterialTheme.shapes] tokens.
 * Miuix mode → miuix components use squircle internally,
 *              custom shapes use [RoundedCornerShape] via [cornerShape].
 */
object SpiceTheme {

    /** Corner radius tokens (dp values, for reference / [cornerShape] fallback). */
    val radiusExtraLarge: Dp = 28.dp
    val radiusLarge:      Dp = 24.dp
    val radiusMedium:     Dp = 16.dp
    val radiusSmall:      Dp = 8.dp

    /**
     * Container shape for large cards and dialogs.
     * Material 3 → [MaterialTheme.shapes.extraLarge] (~28dp)
     */
    @Composable
    fun containerShape(): Shape = MaterialTheme.shapes.extraLarge

    /**
     * Item shape for list items and small buttons.
     * Material 3 → [MaterialTheme.shapes.medium] (~16dp)
     */
    @Composable
    fun itemShape(): Shape = MaterialTheme.shapes.medium

    /**
     * Universal shape accessor — accepts an explicit radius.
     * In Material 3 screens, prefer [containerShape] / [itemShape]
     * so they resolve to Material shapes tokens.
     */
    @Composable
    fun cornerShape(radius: Dp = radiusExtraLarge): Shape = RoundedCornerShape(radius)

    val primary: Color @Composable get() = MaterialTheme.colorScheme.primary
    val onPrimary: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val onSurface: Color @Composable get() = MaterialTheme.colorScheme.onSurface
}
