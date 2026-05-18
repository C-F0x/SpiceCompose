package org.cf0x.spicecompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * Root theme wrapper.
 *
 * Uses miuix as the sole theme for now.
 * When Material3 support is added, this will dispatch on [LocalUiMode].
 */
@Composable
fun SpiceComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()
    MiuixTheme(
        colors = colors,
        content = content,
    )
}
