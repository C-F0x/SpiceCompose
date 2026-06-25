package org.cf0x.spicecompose.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

actual object LocalLocale {
    private val LocalAppLocale = staticCompositionLocalOf { "en" }

    @Composable
    actual infix fun provides(value: String): ProvidedValue<*> {
        return LocalAppLocale provides value
    }
}
