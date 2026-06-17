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
    isM3E:        Boolean                 = false,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()

    val isDark = when (colorMode) {
        ColorMode.LIGHT -> false
        ColorMode.DARK  -> true
        else            -> systemDark
    }

    val isAmoled = isDark && amoledDark

    // Material 3 / You Color Scheme
    // Monet ON  → full dynamic palette from keyColor seed
    // Monet OFF → simple two-tone: accent block colored, rest white/gray
    var m3Scheme = if (useMonet) {
        rememberDynamicColorScheme(
            seedColor   = keyColor,
            isDark      = isDark,
            isAmoled    = isAmoled,
            style       = paletteStyle,
            specVersion = specVersion,
        )
    } else {
        if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary          = keyColor,
                primaryContainer = keyColor.copy(alpha = 0.3f),
                onPrimary        = Color.White,
                onPrimaryContainer = Color.White,
                surface          = Color(0xFF1C1B1F),
                surfaceVariant   = Color(0xFF2D2D2D),
                background       = Color(0xFF1C1B1F),
                outlineVariant   = Color(0xFF3D3D3D),
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary          = keyColor,
                primaryContainer = keyColor.copy(alpha = 0.12f),
                onPrimary        = Color.White,
                onPrimaryContainer = Color.Black,
                surface          = Color.White,
                surfaceVariant   = Color(0xFFF2F2F2),
                background       = Color.White,
                outlineVariant   = Color(0xFFE0E0E0),
            )
        }
    }

    // Force AMOLED black if enabled
    if (isAmoled) {
        m3Scheme = m3Scheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerHigh = Color(0xFF1C1B1F), // Dark enough but visible
            surfaceContainerHighest = Color(0xFF25232A)
        )
    }

    // Miuix compatibility
    val miuixStyle = if (useMonet) paletteStyle else PaletteStyle.TonalSpot
    val miuixPaletteStyle = try {
        ThemePaletteStyle.valueOf(miuixStyle.name)
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
                MaterialTheme(
                    colorScheme = m3Scheme,
                    typography = getTypography(isM3E)
                ) {
                    content()
                }
            }
        }
    }
}
