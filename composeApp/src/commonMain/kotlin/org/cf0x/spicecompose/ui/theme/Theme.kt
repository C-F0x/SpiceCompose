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
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import top.yukonga.miuix.kmp.theme.darkColorScheme

@Composable
fun isInDarkTheme(): Boolean {
    val colorMode = LocalColorMode.current
    return when (colorMode) {
        ColorMode.LIGHT -> false
        ColorMode.DARK  -> true
        else            -> isSystemInDarkTheme()
    }
}

@Composable
fun SpiceComposeTheme(
    colorMode:    ColorMode               = ColorMode.SYSTEM,
    useMonet:     Boolean                 = false,
    amoledDark:   Boolean                 = false,
    keyColor:     Color = defaultKeyColor,
    paletteStyle: PaletteStyle            = PaletteStyle.TonalSpot,
    specVersion:  ColorSpec.SpecVersion   = ColorSpec.SpecVersion.SPEC_2021,
    pageScale:    Float                   = 1.0f,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()

    val isDark = when (colorMode) {
        ColorMode.LIGHT -> false
        ColorMode.DARK  -> true
        else            -> systemDark
    }

    val isAmoled = isDark && amoledDark

    // Material3 color scheme: Monet (wallpaper) takes priority over seed
    val monetScheme = if (useMonet) rememberPlatformMonetScheme(isDark) else null
    val m3Scheme = monetScheme ?: rememberDynamicColorScheme(
        seedColor   = keyColor,
        isDark      = isDark,
        isAmoled    = isAmoled,
        style       = paletteStyle,
        specVersion = specVersion,
    )

    val miuixPaletteStyle = try {
        ThemePaletteStyle.valueOf(paletteStyle.name)
    } catch (_: Exception) {
        ThemePaletteStyle.TonalSpot
    }

    val miuixColorSpec = if (specVersion == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }

    val miuixDarkColors = if (amoledDark) {
        darkColorScheme(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color.Black,
            surfaceContainerHighest = Color.Black,
        )
    } else {
        darkColorScheme()
    }

    val miuixController = ThemeController(
        when {
            (useMonet && colorMode == ColorMode.LIGHT)  -> ColorSchemeMode.MonetLight
            (useMonet && colorMode == ColorMode.DARK)   -> ColorSchemeMode.MonetDark
            useMonet                                    -> ColorSchemeMode.MonetSystem
            colorMode == ColorMode.LIGHT                -> ColorSchemeMode.Light
            colorMode == ColorMode.DARK                 -> ColorSchemeMode.Dark
            else                                        -> ColorSchemeMode.System
        },
        darkColors = miuixDarkColors,
        keyColor = keyColor,
        isDark = isDark,
        paletteStyle = miuixPaletteStyle,
        colorSpec = miuixColorSpec,
    )

    // ── Apply page scale by overriding LocalDensity ──────────────────────────
    val baseDensity = LocalDensity.current
    val scaledDensity = if (pageScale != 1.0f)
        Density(baseDensity.density * pageScale, baseDensity.fontScale)
    else
        baseDensity

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MiuixTheme(controller = miuixController) {
            CompositionLocalProvider(
                top.yukonga.miuix.kmp.theme.LocalContentColor provides MiuixTheme.colorScheme.onBackground,
            ) {
                MaterialTheme(colorScheme = m3Scheme) {
                    content()
                }
            }
        }
    }
}
