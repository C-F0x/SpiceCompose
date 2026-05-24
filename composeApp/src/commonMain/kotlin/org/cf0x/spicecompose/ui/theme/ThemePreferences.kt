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

    private var _uiMode by mutableStateOf(UiMode.entries.getOrElse(s.getInt("uiMode", 0)) { UiMode.Miuix })
    val uiMode: UiMode get() = _uiMode
    fun setUiMode(v: UiMode) { _uiMode = v; s.putInt("uiMode", v.ordinal) }

    private var _appLanguage by mutableStateOf(AppLanguage.entries.getOrElse(s.getInt("appLanguage", 0)) { AppLanguage.EN })
    val appLanguage: AppLanguage get() = _appLanguage
    fun setAppLanguage(v: AppLanguage) { _appLanguage = v; s.putInt("appLanguage", v.ordinal) }

    private var _colorMode by mutableStateOf(ColorMode.fromValue(s.getInt("colorMode", ColorMode.SYSTEM.value)))
    val colorMode: ColorMode get() = _colorMode
    fun setColorMode(v: ColorMode) { _colorMode = v; s.putInt("colorMode", v.value) }

    private var _keyColor by mutableStateOf(Color(s.getInt("keyColor", defaultKeyColor.toArgb())))
    val keyColor: Color get() = _keyColor
    fun setKeyColor(v: Color) { _keyColor = v; s.putInt("keyColor", v.toArgb()) }

    private var _paletteStyle by mutableStateOf(PaletteStyle.entries.getOrElse(s.getInt("paletteStyle", 0)) { PaletteStyle.TonalSpot })
    val paletteStyle: PaletteStyle get() = _paletteStyle
    fun setPaletteStyle(v: PaletteStyle) { _paletteStyle = v; s.putInt("paletteStyle", v.ordinal) }

    private var _colorSpecVersion by mutableStateOf(
        if (s.getInt("colorSpec", 0) == 1) ColorSpec.SpecVersion.SPEC_2025 else ColorSpec.SpecVersion.SPEC_2021)
    val colorSpecVersion: ColorSpec.SpecVersion get() = _colorSpecVersion
    fun setColorSpecVersion(v: ColorSpec.SpecVersion) {
        _colorSpecVersion = v; s.putInt("colorSpec", if (v == ColorSpec.SpecVersion.SPEC_2025) 1 else 0)
    }

    private var _navLayoutMode by mutableStateOf(NavLayoutMode.entries.getOrElse(s.getInt("navLayoutMode", 0)) { NavLayoutMode.Auto })
    val navLayoutMode: NavLayoutMode get() = _navLayoutMode
    fun setNavLayoutMode(v: NavLayoutMode) { _navLayoutMode = v; s.putInt("navLayoutMode", v.ordinal) }

    private var _pageScale by mutableFloatStateOf(s.getFloat("pageScale", 1.0f))
    val pageScale: Float get() = _pageScale
    fun setPageScale(v: Float) { _pageScale = v; s.putFloat("pageScale", v) }

    private var _floatingBottomBar by mutableStateOf(s.getBoolean("floatingBottomBar", false))
    val floatingBottomBar: Boolean get() = _floatingBottomBar
    fun setFloatingBottomBar(v: Boolean) { _floatingBottomBar = v; s.putBoolean("floatingBottomBar", v) }

    private var _floatingBottomBarBlur by mutableStateOf(s.getBoolean("floatingBottomBarBlur", true))
    val floatingBottomBarBlur: Boolean get() = _floatingBottomBarBlur
    fun setFloatingBottomBarBlur(v: Boolean) { _floatingBottomBarBlur = v; s.putBoolean("floatingBottomBarBlur", v) }

    private var _enableBlur by mutableStateOf(s.getBoolean("enableBlur", true))
    val enableBlur: Boolean get() = _enableBlur
    fun setEnableBlur(v: Boolean) { _enableBlur = v; s.putBoolean("enableBlur", v) }

    private var _enableSmoothCorner by mutableStateOf(s.getBoolean("enableSmoothCorner", true))
    val enableSmoothCorner: Boolean get() = _enableSmoothCorner
    fun setEnableSmoothCorner(v: Boolean) { _enableSmoothCorner = v; s.putBoolean("enableSmoothCorner", v) }

    private var _predictiveBack by mutableStateOf(s.getBoolean("predictiveBack", true))
    val predictiveBack: Boolean get() = _predictiveBack
    fun setPredictiveBack(v: Boolean) { _predictiveBack = v; s.putBoolean("predictiveBack", v) }
}
