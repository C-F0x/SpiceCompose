package org.cf0x.spicecompose.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

actual object LocalLocale {
    private val LocalAppLocale = staticCompositionLocalOf { Locale.getDefault().toString() }

    @Composable
    actual infix fun provides(value: String): ProvidedValue<*> {
        val locale = if (value.contains("-r")) {
            val parts = value.split("-r")
            Locale(parts[0], parts[1])
        } else {
            Locale(value)
        }
        Locale.setDefault(locale)
        return LocalAppLocale provides value
    }
}
