package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.theme.ColorMode

@ExperimentalMaterial3Api
@Composable
fun ThemeScreen(
    colorMode: ColorMode,            onColorModeChange: (ColorMode) -> Unit,
    useMonet: Boolean,               onUseMonetChange: (Boolean) -> Unit,
    amoledDark: Boolean,             onAmoledDarkChange: (Boolean) -> Unit,
    keyColor: Color,                 onKeyColorChange: (Color) -> Unit,
    paletteStyle: PaletteStyle,      onPaletteStyleChange: (PaletteStyle) -> Unit,
    colorSpecVersion: ColorSpec.SpecVersion, onColorSpecVersionChange: (ColorSpec.SpecVersion) -> Unit,
    navLayoutMode: NavLayoutMode,    onNavLayoutModeChange: (NavLayoutMode) -> Unit,
    pageScale: Float,                onPageScaleChange: (Float) -> Unit,
    floatingBottomBar: Boolean,      onFloatingBottomBarChange: (Boolean) -> Unit,
    floatingBottomBarBlur: Boolean,  onFloatingBottomBarBlurChange: (Boolean) -> Unit,
    enableBlur: Boolean,             onEnableBlurChange: (Boolean) -> Unit,
    enableSmoothCorner: Boolean,     onEnableSmoothCornerChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val uiState = ThemeUiState(
        colorMode = colorMode, useMonet = useMonet, amoledDark = amoledDark,
        keyColor = keyColor, paletteStyle = paletteStyle,
        colorSpecVersion = colorSpecVersion, navLayoutMode = navLayoutMode, pageScale = pageScale,
        floatingBottomBar = floatingBottomBar, floatingBottomBarBlur = floatingBottomBarBlur,
        enableBlur = enableBlur, enableSmoothCorner = enableSmoothCorner,
    )
    val actions = ThemeScreenActions(
        onBack = onBack, onSetColorMode = onColorModeChange, 
        onSetUseMonet = onUseMonetChange, onSetAmoledDark = onAmoledDarkChange,
        onSetKeyColor = onKeyColorChange,
        onSetPaletteStyle = onPaletteStyleChange, onSetColorSpecVersion = onColorSpecVersionChange,
        onSetNavLayoutMode = onNavLayoutModeChange, onSetPageScale = onPageScaleChange,
        onSetFloatingBottomBar = onFloatingBottomBarChange,
        onSetFloatingBottomBarBlur = onFloatingBottomBarBlurChange,
        onSetEnableBlur = onEnableBlurChange, onSetEnableSmoothCorner = onEnableSmoothCornerChange,
    )
    when (LocalUiMode.current) {
        UiMode.Miuix    -> ThemeScreenMiuix(uiState, actions)
        UiMode.Material -> ThemeScreenMaterial(uiState, actions)
    }
}
