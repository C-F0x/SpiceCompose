package org.cf0x.spicecompose.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Global design token accessor.
 */
object SpiceTheme {

    /** Corner radius tokens. */
    val radiusExtraLarge: Dp = 28.dp
    val radiusLarge:      Dp = 24.dp
    val radiusMedium:     Dp = 16.dp
    val radiusSmall:      Dp = 8.dp

    /**
     * Returns the appropriate shape for Containers (Large cards, Dialogs).
     * MD3: 28dp, M3E: Circle (Capsule)
     */
    @Composable
    fun containerShape(): Shape {
        val isM3E = LocalEnableSmoothCorner.current
        return if (isM3E) CircleShape else RoundedCornerShape(radiusExtraLarge)
    }

    /**
     * Returns the appropriate shape for Items (List items, Small buttons).
     * MD3: 16dp, M3E: Circle (Capsule)
     */
    @Composable
    fun itemShape(): Shape {
        val isM3E = LocalEnableSmoothCorner.current
        return if (isM3E) CircleShape else RoundedCornerShape(radiusMedium)
    }

    /** Legacy / Universal accessor */
    @Composable
    fun cornerShape(radius: Dp = radiusExtraLarge): Shape {
        val isM3E = LocalEnableSmoothCorner.current
        return if (isM3E) CircleShape else RoundedCornerShape(radius)
    }

    val primary: Color @Composable get() = MaterialTheme.colorScheme.primary
    val onPrimary: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val onSurface: Color @Composable get() = MaterialTheme.colorScheme.onSurface
}
