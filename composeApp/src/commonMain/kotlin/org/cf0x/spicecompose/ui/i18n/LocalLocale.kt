package org.cf0x.spicecompose.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

expect object LocalLocale {
    @Composable
    infix fun provides(value: String): ProvidedValue<*>
}
