package org.cf0x.spicecompose.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

// ── Color ─────────────────────────────────────────────────────────────────────
val LocalColorMode          = staticCompositionLocalOf { ColorMode.SYSTEM }
val LocalKeyColor           = staticCompositionLocalOf { defaultKeyColor }
val LocalPaletteStyle       = staticCompositionLocalOf { PaletteStyle.TonalSpot }
val LocalColorSpecVersion   = staticCompositionLocalOf { ColorSpec.SpecVersion.SPEC_2021 }

// ── Layout ────────────────────────────────────────────────────────────────────
val LocalPageScale              = staticCompositionLocalOf { 1.0f }
val LocalFloatingBottomBar      = staticCompositionLocalOf { false }
val LocalFloatingBottomBarBlur  = staticCompositionLocalOf { true }

// ── Effects ───────────────────────────────────────────────────────────────────
val LocalEnableBlur         = staticCompositionLocalOf { true }
val LocalEnableSmoothCorner = staticCompositionLocalOf { true }
