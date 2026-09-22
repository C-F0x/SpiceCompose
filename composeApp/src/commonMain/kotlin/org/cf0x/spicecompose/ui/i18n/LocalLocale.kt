package org.cf0x.spicecompose.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

expect object LocalLocale {
    @Composable
    infix fun provides(value: String): ProvidedValue<*>
}

/**
 * Applies the selected application language to the platform resource system.
 *
 * Most targets only need the existing CompositionLocal. iOS additionally has
 * to override Compose Resources' system-locale environment.
 */
@Composable
expect fun ProvideAppLocale(value: String, content: @Composable () -> Unit)
