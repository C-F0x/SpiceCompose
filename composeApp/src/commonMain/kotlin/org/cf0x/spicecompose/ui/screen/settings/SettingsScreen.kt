package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.screen.about.AboutScreen
import org.cf0x.spicecompose.ui.screen.theme.ThemeScreen
import org.cf0x.spicecompose.ui.theme.ColorMode
import androidx.compose.material3.ExperimentalMaterial3Api
import org.cf0x.spicecompose.ui.SpiceBackHandler

private const val ROUTE_MAIN  = "main"
private const val ROUTE_THEME = "theme"
private const val ROUTE_ABOUT = "about"

@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    uiMode: UiMode,                  onUiModeChange: (UiMode) -> Unit,
    appLanguage: AppLanguage,        onLanguageChange: (AppLanguage) -> Unit,
    colorMode: ColorMode,            onColorModeChange: (ColorMode) -> Unit,
    keyColor: Color,                 onKeyColorChange: (Color) -> Unit,
    paletteStyle: PaletteStyle,      onPaletteStyleChange: (PaletteStyle) -> Unit,
    colorSpecVersion: ColorSpec.SpecVersion, onColorSpecVersionChange: (ColorSpec.SpecVersion) -> Unit,
    navLayoutMode: NavLayoutMode,    onNavLayoutModeChange: (NavLayoutMode) -> Unit,
    pageScale: Float,                onPageScaleChange: (Float) -> Unit,
    floatingBottomBar: Boolean,      onFloatingBottomBarChange: (Boolean) -> Unit,
    floatingBottomBarBlur: Boolean,  onFloatingBottomBarBlurChange: (Boolean) -> Unit,
    enableBlur: Boolean,             onEnableBlurChange: (Boolean) -> Unit,
    enableSmoothCorner: Boolean,     onEnableSmoothCornerChange: (Boolean) -> Unit,
) {
    var route by rememberSaveable { mutableStateOf(ROUTE_MAIN) }

    // Notify MainScreen to disable pager swipe when in a sub-page
    val inSubPage = LocalInSubPage.current
    SideEffect { inSubPage.value = route != ROUTE_MAIN }

    // Intercept back gesture on sub-pages only
    SpiceBackHandler(enabled = route != ROUTE_MAIN) { route = ROUTE_MAIN }

    when (route) {
        ROUTE_THEME -> ThemeScreen(
            colorMode = colorMode,               onColorModeChange = onColorModeChange,
            keyColor = keyColor,                 onKeyColorChange = onKeyColorChange,
            paletteStyle = paletteStyle,         onPaletteStyleChange = onPaletteStyleChange,
            colorSpecVersion = colorSpecVersion, onColorSpecVersionChange = onColorSpecVersionChange,
            navLayoutMode = navLayoutMode,       onNavLayoutModeChange = onNavLayoutModeChange,
            pageScale = pageScale,               onPageScaleChange = onPageScaleChange,
            floatingBottomBar = floatingBottomBar, onFloatingBottomBarChange = onFloatingBottomBarChange,
            floatingBottomBarBlur = floatingBottomBarBlur, onFloatingBottomBarBlurChange = onFloatingBottomBarBlurChange,
            enableBlur = enableBlur,             onEnableBlurChange = onEnableBlurChange,
            enableSmoothCorner = enableSmoothCorner, onEnableSmoothCornerChange = onEnableSmoothCornerChange,
            onBack = { route = ROUTE_MAIN },
        )
        ROUTE_ABOUT -> AboutScreen(onBack = { route = ROUTE_MAIN })
        else -> {
            val uiState = SettingsUiState(language = appLanguage, uiMode = uiMode)
            val actions = SettingsScreenActions(
                onSetLanguage    = onLanguageChange,
                onSetUiModeIndex = { onUiModeChange(if (it == 0) UiMode.Miuix else UiMode.Material) },
                onOpenTheme      = { route = ROUTE_THEME },
                onOpenAbout      = { route = ROUTE_ABOUT },
            )
            when (LocalUiMode.current) {
                UiMode.Miuix    -> SettingsPagerMiuix(uiState, actions)
                UiMode.Material -> SettingsPagerMaterial(uiState, actions)
            }
        }
    }
}
