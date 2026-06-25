package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.screen.about.AboutScreen
import org.cf0x.spicecompose.ui.screen.settings.ControllerFaqScreen
import org.cf0x.spicecompose.ui.screen.theme.CustomizeScreen
import org.cf0x.spicecompose.ui.theme.ColorMode
import androidx.compose.material3.ExperimentalMaterial3Api
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.i18n.isSystemLocaleOverridden

private const val ROUTE_MAIN  = "main"
private const val ROUTE_THEME = "theme"
private const val ROUTE_ABOUT = "about"
private const val ROUTE_FAQ   = "faq"

@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    uiMode: UiMode,                  onUiModeChange: (UiMode) -> Unit,
    appLanguage: AppLanguage,        onLanguageChange: (AppLanguage) -> Unit,
    colorMode: ColorMode,            onColorModeChange: (ColorMode) -> Unit,
    useMonet: Boolean,               onUseMonetChange: (Boolean) -> Unit,
    amoledDark: Boolean,             onAmoledDarkChange: (Boolean) -> Unit,
    keyColor: Color,                 onKeyColorChange: (Color) -> Unit,
    materialKeyColor: Color,          onMaterialKeyColorChange: (Color) -> Unit,
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
    val mainState = LocalMainPagerState.current

    // Handle reset events from BottomBar
    LaunchedEffect(mainState) {
        mainState.resetEvents
            .filter { it == Destination.Settings.index }
            .collect { route = ROUTE_MAIN }
    }

    // Notify MainScreen to disable pager swipe when in a sub-page
    val inSubPage = LocalInSubPage.current
    inSubPage.value = route != ROUTE_MAIN

    // Intercept back gesture on sub-pages only
    SpiceBackHandler(enabled = route != ROUTE_MAIN) { route = ROUTE_MAIN }

    AnimatedContent(
        targetState = route,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { currentRoute ->
        when (currentRoute) {
        ROUTE_THEME -> CustomizeScreen(
            colorMode = colorMode,               onColorModeChange = onColorModeChange,
            useMonet = useMonet,                 onUseMonetChange = onUseMonetChange,
            amoledDark = amoledDark,             onAmoledDarkChange = onAmoledDarkChange,
            keyColor = keyColor,                 onKeyColorChange = onKeyColorChange,
            materialKeyColor = materialKeyColor,  onMaterialKeyColorChange = onMaterialKeyColorChange,
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
        ROUTE_ABOUT -> AboutScreen(onBack = { route = ROUTE_MAIN }, onOpenFaq = { route = ROUTE_FAQ })
        ROUTE_FAQ   -> ControllerFaqScreen(onBack = { route = ROUTE_MAIN })
        else -> {
            val systemLocaleOverridden = isSystemLocaleOverridden()
            val uiState = SettingsUiState(language = appLanguage, uiMode = uiMode, systemLocaleOverridden = systemLocaleOverridden)
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
}
