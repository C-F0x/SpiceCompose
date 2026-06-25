package org.cf0x.spicecompose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.platform.SystemBarsManager
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.i18n.LocalLocale
import org.cf0x.spicecompose.ui.i18n.appStrings
import org.cf0x.spicecompose.ui.screen.MainScreen
import org.cf0x.spicecompose.ui.screen.settings.SettingsScreen
import org.cf0x.spicecompose.ui.theme.*

@ExperimentalMaterial3Api
@Composable
fun App() {
    val p       = ThemePreferences

    // Shared state: true when a sub-page (ThemeScreen / AboutScreen) is shown.
    // Passed down via CompositionLocal so MainScreen can disable pager swipe.
    val inSubPage = remember { mutableStateOf(false) }
    val connectionManager = remember { ConnectionManager() }
    val fullscreenMode = remember { mutableStateOf(false) }
    val isMaterial = p.uiMode == org.cf0x.spicecompose.ui.UiMode.Material
    val effectiveKeyColor = if (isMaterial) p.materialKeyColor else p.keyColor

    LaunchedEffect(fullscreenMode.value) {
        SystemBarsManager.setFullscreen(fullscreenMode.value)
    }

    // Apply locale at the platform level (Android Configuration / Desktop / Wasm)
    CompositionLocalProvider(
        LocalLocale provides p.appLanguage.isoCode,
        LocalUiMode provides p.uiMode,
    ) {
        // Read strings from XML resources — recomposed when p.appLanguage changes
        // because LocalLocale re-triggers the Compose tree via Configuration update
        val strings = appStrings()
        CompositionLocalProvider(
            LocalAppStrings            provides strings,
            LocalColorMode             provides p.colorMode,
            LocalKeyColor              provides effectiveKeyColor,
            LocalPaletteStyle          provides p.paletteStyle,
            LocalColorSpecVersion      provides p.colorSpecVersion,
            LocalEnableBlur            provides p.enableBlur,
            LocalEnableSmoothCorner    provides p.enableSmoothCorner,
            LocalPageScale             provides p.pageScale,
            LocalFloatingBottomBar     provides p.floatingBottomBar,
            LocalFloatingBottomBarBlur provides p.floatingBottomBarBlur,
            LocalInSubPage             provides inSubPage,
            LocalConnectionManager     provides connectionManager,
            LocalFullscreenMode        provides fullscreenMode,
        ) {
            SpiceComposeTheme(
                colorMode    = p.colorMode,
                useMonet     = isMaterial || p.useMonet,
                amoledDark   = p.amoledDark,
                keyColor     = effectiveKeyColor,
                paletteStyle = p.paletteStyle,
                specVersion  = p.colorSpecVersion,
                pageScale    = p.pageScale,
                isM3E        = p.enableSmoothCorner,
            ) {
                MainScreen(
                    navLayoutMode         = p.navLayoutMode,
                    onNavLayoutModeChange = { p.updateNavLayoutMode(it) },
                    settingsContent = {
                        SettingsScreen(
                            uiMode                        = p.uiMode,
                            onUiModeChange                = { p.updateUiMode(it); if (it == org.cf0x.spicecompose.ui.UiMode.Material) p.updateFloatingBottomBar(false) },
                            appLanguage                   = p.appLanguage,
                            onLanguageChange              = { p.updateAppLanguage(it) },
                            colorMode                     = p.colorMode,
                            onColorModeChange             = { p.updateColorMode(it) },
                            useMonet                      = p.useMonet,
                            onUseMonetChange              = { p.updateUseMonet(it) },
                            amoledDark                    = p.amoledDark,
                            onAmoledDarkChange            = { p.updateAmoledDark(it) },
                            keyColor                      = p.keyColor,
                            onKeyColorChange              = { p.updateKeyColor(it) },
                            materialKeyColor              = p.materialKeyColor,
                            onMaterialKeyColorChange      = { p.updateMaterialKeyColor(it) },
                            paletteStyle                  = p.paletteStyle,
                            onPaletteStyleChange          = { p.updatePaletteStyle(it) },
                            colorSpecVersion              = p.colorSpecVersion,
                            onColorSpecVersionChange      = { p.updateColorSpecVersion(it) },
                            pageScale                     = p.pageScale,
                            onPageScaleChange             = { p.updatePageScale(it) },
                            navLayoutMode                 = p.navLayoutMode,
                            onNavLayoutModeChange         = { p.updateNavLayoutMode(it) },
                            floatingBottomBar             = p.floatingBottomBar,
                            onFloatingBottomBarChange     = { p.updateFloatingBottomBar(it) },
                            floatingBottomBarBlur         = p.floatingBottomBarBlur,
                            onFloatingBottomBarBlurChange = { p.updateFloatingBottomBarBlur(it) },
                            enableBlur                    = p.enableBlur,
                            onEnableBlurChange            = { p.updateEnableBlur(it) },
                            enableSmoothCorner            = p.enableSmoothCorner,
                            onEnableSmoothCornerChange    = { p.updateEnableSmoothCorner(it) },
                        )
                    },
                )
            }
        }
    }
}
