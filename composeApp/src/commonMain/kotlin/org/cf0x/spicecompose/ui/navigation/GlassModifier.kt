package org.cf0x.spicecompose.ui.navigation

import androidx.compose.ui.Modifier

/**
 * Applies a liquid-glass / frosted-glass effect to the modifier chain.
 *
 * - Android 12+ (API 31): uses RenderEffect background blur
 * - Older Android / Desktop: semi-transparent surface fallback
 *
 * Set [enabled] = false to get a fully opaque surface instead.
 */
expect fun Modifier.liquidGlass(enabled: Boolean = true): Modifier
