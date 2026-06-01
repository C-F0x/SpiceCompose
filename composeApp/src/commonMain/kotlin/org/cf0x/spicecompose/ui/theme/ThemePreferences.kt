package org.cf0x.spicecompose.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.russhwolf.settings.Settings
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode

object ThemePreferences {
    private val s: Settings by lazy { Settings() }

    var uiMode by mutableStateOf(UiMode.entries.getOrElse(s.getInt("uiMode", 0)) { UiMode.Miuix })
        private set
    fun updateUiMode(v: UiMode) { uiMode = v; s.putInt("uiMode", v.ordinal) }

    var appLanguage by mutableStateOf(AppLanguage.entries.getOrElse(s.getInt("appLanguage", 0)) { AppLanguage.EN })
        private set
    fun updateAppLanguage(v: AppLanguage) { appLanguage = v; s.putInt("appLanguage", v.ordinal) }

    var colorMode by mutableStateOf(ColorMode.fromValue(s.getInt("colorMode", ColorMode.SYSTEM.value)))
        private set
    fun updateColorMode(v: ColorMode) { colorMode = v; s.putInt("colorMode", v.value) }

    var useMonet by mutableStateOf(s.getBoolean("useMonet", false))
        private set
    fun updateUseMonet(v: Boolean) { useMonet = v; s.putBoolean("useMonet", v) }

    var amoledDark by mutableStateOf(s.getBoolean("amoledDark", false))
        private set
    fun updateAmoledDark(v: Boolean) { amoledDark = v; s.putBoolean("amoledDark", v) }

    var keyColor by mutableStateOf(Color(s.getInt("keyColor", defaultKeyColor.toArgb())))
        private set
    fun updateKeyColor(v: Color) { keyColor = v; s.putInt("keyColor", v.toArgb()) }

    var paletteStyle by mutableStateOf(PaletteStyle.entries.getOrElse(s.getInt("paletteStyle", 0)) { PaletteStyle.TonalSpot })
        private set
    fun updatePaletteStyle(v: PaletteStyle) { paletteStyle = v; s.putInt("paletteStyle", v.ordinal) }

    var colorSpecVersion by mutableStateOf(
        if (s.getInt("colorSpec", 0) == 1) ColorSpec.SpecVersion.SPEC_2025 else ColorSpec.SpecVersion.SPEC_2021)
        private set
    fun updateColorSpecVersion(v: ColorSpec.SpecVersion) {
        colorSpecVersion = v; s.putInt("colorSpec", if (v == ColorSpec.SpecVersion.SPEC_2025) 1 else 0)
    }

    var navLayoutMode by mutableStateOf(NavLayoutMode.entries.getOrElse(s.getInt("navLayoutMode", 0)) { NavLayoutMode.Auto })
        private set
    fun updateNavLayoutMode(v: NavLayoutMode) { navLayoutMode = v; s.putInt("navLayoutMode", v.ordinal) }

    var pageScale by mutableFloatStateOf(s.getFloat("pageScale", 1.0f))
        private set
    fun updatePageScale(v: Float) { pageScale = v; s.putFloat("pageScale", v) }

    var floatingBottomBar by mutableStateOf(s.getBoolean("floatingBottomBar", false))
        private set
    fun updateFloatingBottomBar(v: Boolean) { floatingBottomBar = v; s.putBoolean("floatingBottomBar", v) }

    var floatingBottomBarBlur by mutableStateOf(s.getBoolean("floatingBottomBarBlur", true))
        private set
    fun updateFloatingBottomBarBlur(v: Boolean) { floatingBottomBarBlur = v; s.putBoolean("floatingBottomBarBlur", v) }

    var enableBlur by mutableStateOf(s.getBoolean("enableBlur", true))
        private set
    fun updateEnableBlur(v: Boolean) { enableBlur = v; s.putBoolean("enableBlur", v) }

    var enableSmoothCorner by mutableStateOf(s.getBoolean("enableSmoothCorner", true))
        private set
    fun updateEnableSmoothCorner(v: Boolean) { enableSmoothCorner = v; s.putBoolean("enableSmoothCorner", v) }
}
