package org.cf0x.spicecompose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.i18n.EnStrings
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.i18n.ZhCnStrings
import org.cf0x.spicecompose.ui.screen.MainScreen
import org.cf0x.spicecompose.ui.screen.settings.SettingsScreen
import org.cf0x.spicecompose.ui.theme.*

@ExperimentalMaterial3Api
@Composable
fun App() {
    val p = ThemePreferences
    val strings = when (p.appLanguage) { AppLanguage.ZH_CN -> ZhCnStrings; else -> EnStrings }

    CompositionLocalProvider(
        LocalUiMode              provides p.uiMode,
        LocalAppStrings          provides strings,
        LocalColorMode           provides p.colorMode,
        LocalKeyColor            provides p.keyColor,
        LocalPaletteStyle        provides p.paletteStyle,
        LocalColorSpecVersion    provides p.colorSpecVersion,
        LocalEnableBlur          provides p.enableBlur,
        LocalEnableSmoothCorner  provides p.enableSmoothCorner,
        LocalPageScale           provides p.pageScale,
        LocalFloatingBottomBar   provides p.floatingBottomBar,
        LocalFloatingBottomBarBlur provides p.floatingBottomBarBlur,
    ) {
        SpiceComposeTheme(
            colorMode    = p.colorMode,
            keyColor     = p.keyColor,
            paletteStyle = p.paletteStyle,
            specVersion  = p.colorSpecVersion,
            pageScale    = p.pageScale,
        ) {
            MainScreen(
                navLayoutMode         = p.navLayoutMode,
                onNavLayoutModeChange = { p.setNavLayoutMode(it) },
                settingsContent = {
                    SettingsScreen(
                        uiMode                        = p.uiMode,
                        onUiModeChange                = { p.setUiMode(it) },
                        appLanguage                   = p.appLanguage,
                        onLanguageChange              = { p.setAppLanguage(it) },
                        colorMode                     = p.colorMode,
                        onColorModeChange             = { p.setColorMode(it) },
                        keyColor                      = p.keyColor,
                        onKeyColorChange              = { p.setKeyColor(it) },
                        paletteStyle                  = p.paletteStyle,
                        onPaletteStyleChange          = { p.setPaletteStyle(it) },
                        colorSpecVersion              = p.colorSpecVersion,
                        onColorSpecVersionChange      = { p.setColorSpecVersion(it) },
                        navLayoutMode                 = p.navLayoutMode,
                        onNavLayoutModeChange         = { p.setNavLayoutMode(it) },
                        pageScale                     = p.pageScale,
                        onPageScaleChange             = { p.setPageScale(it) },
                        floatingBottomBar             = p.floatingBottomBar,
                        onFloatingBottomBarChange     = { p.setFloatingBottomBar(it) },
                        floatingBottomBarBlur         = p.floatingBottomBarBlur,
                        onFloatingBottomBarBlurChange = { p.setFloatingBottomBarBlur(it) },
                        enableBlur                    = p.enableBlur,
                        onEnableBlurChange            = { p.setEnableBlur(it) },
                        enableSmoothCorner            = p.enableSmoothCorner,
                        onEnableSmoothCornerChange    = { p.setEnableSmoothCorner(it) },
                        predictiveBack                = p.predictiveBack,
                        onPredictiveBackChange        = { p.setPredictiveBack(it) },
                    )
                },
            )
        }
    }
}
