package org.cf0x.spicecompose.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** Returns a system wallpaper-based ColorScheme on Android 12+, null elsewhere. */
@Composable
expect fun rememberPlatformMonetScheme(isDark: Boolean): ColorScheme?
