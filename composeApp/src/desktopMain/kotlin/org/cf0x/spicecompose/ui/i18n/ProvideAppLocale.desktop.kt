package org.cf0x.spicecompose.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
actual fun ProvideAppLocale(value: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLocale provides value, content = content)
}
