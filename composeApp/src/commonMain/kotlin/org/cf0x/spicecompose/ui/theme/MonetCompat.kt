package org.cf0x.spicecompose.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Returns a system wallpaper-based ColorScheme on Android 12+, null elsewhere. */
@Composable
expect fun rememberPlatformMonetScheme(isDark: Boolean): ColorScheme?

/** Returns the system wallpaper accent color on Android 12+, defaultKeyColor elsewhere. */
@Composable
expect fun rememberSystemAccentColor(): Color
