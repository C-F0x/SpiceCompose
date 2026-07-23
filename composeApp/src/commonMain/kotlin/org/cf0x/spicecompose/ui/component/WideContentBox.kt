package org.cf0x.spicecompose.ui.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize

/**
 * Max content width for wide screens.
 * Content exceeding this is padded inward on wide screens.
 */
private val MaxContentWidth = 800.dp

/**
 * Wraps the content in a horizontally centered box on wide screens (≥600dp).
 * On compact phones the content fills the full width unchanged.
 *
 * Usage:
 * ```
 * WideContentBox { sidePadding ->
 *     LazyColumn(contentPadding = PaddingValues(top = t, start = sidePadding, end = sidePadding))
 * }
 * ```
 */
@Composable
fun WideContentBox(
    content: @Composable (sidePadding: Dp) -> Unit,
) {
    val isWide = LocalWindowSize.current != WindowSize.Compact

    if (isWide) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val sidePadding = ((maxWidth - MaxContentWidth) / 2).coerceAtLeast(0.dp)
            content(sidePadding)
        }
    } else {
        content(0.dp)
    }
}
