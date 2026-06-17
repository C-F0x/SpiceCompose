package org.cf0x.spicecompose.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalUiMode             = staticCompositionLocalOf { UiMode.Miuix }
val LocalColorMode          = staticCompositionLocalOf { ColorMode.SYSTEM }
val LocalKeyColor           = staticCompositionLocalOf { defaultKeyColor }
val LocalPaletteStyle       = staticCompositionLocalOf { com.materialkolor.PaletteStyle.TonalSpot }
val LocalColorSpecVersion   = staticCompositionLocalOf { com.materialkolor.dynamiccolor.ColorSpec.SpecVersion.SPEC_2021 }
val LocalEnableBlur         = staticCompositionLocalOf { true }
val LocalEnableSmoothCorner = staticCompositionLocalOf { false }
val LocalPageScale          = staticCompositionLocalOf { 1.0f }

val LocalFloatingBottomBar      = staticCompositionLocalOf { false }
val LocalFloatingBottomBarBlur  = staticCompositionLocalOf { true }

// New Local to pass the required bottom padding to sub-pages to avoid the floating bar
val LocalBottomBarPadding = staticCompositionLocalOf { 0.dp }

enum class UiMode { Miuix, Material }
