package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.theme.ColorMode
import org.cf0x.spicecompose.ui.theme.defaultKeyColor

@Immutable
data class ThemeUiState(
    val colorMode:             ColorMode             = ColorMode.SYSTEM,
    val keyColor:              Color                 = defaultKeyColor,
    val paletteStyle:          PaletteStyle          = PaletteStyle.TonalSpot,
    val colorSpecVersion:      ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2021,
    val navLayoutMode:         NavLayoutMode         = NavLayoutMode.Auto,
    val pageScale:             Float                 = 1.0f,
    val floatingBottomBar:     Boolean               = false,
    val floatingBottomBarBlur: Boolean               = true,
    val enableBlur:            Boolean               = true,
    val enableSmoothCorner:    Boolean               = true,
    // predictiveBack removed: controlled by AndroidManifest, not runtime-toggleable
)

@Immutable
data class ThemeScreenActions(
    val onBack:                         () -> Unit,
    val onSetColorMode:                 (ColorMode) -> Unit,
    val onSetKeyColor:                  (Color) -> Unit,
    val onSetPaletteStyle:              (PaletteStyle) -> Unit,
    val onSetColorSpecVersion:          (ColorSpec.SpecVersion) -> Unit,
    val onSetNavLayoutMode:             (NavLayoutMode) -> Unit,
    val onSetPageScale:                 (Float) -> Unit,
    val onSetFloatingBottomBar:         (Boolean) -> Unit,
    val onSetFloatingBottomBarBlur:     (Boolean) -> Unit,
    val onSetEnableBlur:                (Boolean) -> Unit,
    val onSetEnableSmoothCorner:        (Boolean) -> Unit,
)
