package org.cf0x.spicecompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

@Composable
fun isInDarkTheme(): Boolean {
    val colorMode = LocalColorMode.current
    val systemDark = isSystemInDarkTheme()
    return when {
        colorMode.isAmoled -> true
        colorMode.isDark   -> true
        colorMode.isLight  -> false
        colorMode == ColorMode.MONET_LIGHT -> false
        colorMode == ColorMode.MONET_DARK  -> true
        else               -> systemDark
    }
}

@Composable
fun SpiceComposeTheme(
    colorMode:    ColorMode               = ColorMode.SYSTEM,
    keyColor:     androidx.compose.ui.graphics.Color = defaultKeyColor,
    paletteStyle: PaletteStyle            = PaletteStyle.TonalSpot,
    specVersion:  ColorSpec.SpecVersion   = ColorSpec.SpecVersion.SPEC_2021,
    pageScale:    Float                   = 1.0f,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()

    val isDark = when {
        colorMode.isAmoled -> true
        colorMode.isDark   -> true
        colorMode.isLight  -> false
        colorMode == ColorMode.MONET_LIGHT -> false
        colorMode == ColorMode.MONET_DARK  -> true
        else               -> systemDark   // SYSTEM / MONET_SYSTEM
    }

    // Material3 color scheme: Monet (wallpaper) takes priority over seed
    val monetScheme = if (colorMode.isMonet) rememberPlatformMonetScheme(isDark) else null
    val m3Scheme = monetScheme ?: rememberDynamicColorScheme(
        seedColor   = keyColor,
        isDark      = isDark,
        isAmoled    = colorMode.isAmoled,
        style       = paletteStyle,
        specVersion = specVersion,
    )

    val miuixColors = if (isDark) darkColorScheme() else lightColorScheme()

    // ── Apply page scale by overriding LocalDensity ──────────────────────────
    val baseDensity = LocalDensity.current
    val scaledDensity = if (pageScale != 1.0f)
        Density(baseDensity.density * pageScale, baseDensity.fontScale)
    else
        baseDensity

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MiuixTheme(colors = miuixColors) {
            MaterialTheme(colorScheme = m3Scheme) {
                content()
            }
        }
    }
}
