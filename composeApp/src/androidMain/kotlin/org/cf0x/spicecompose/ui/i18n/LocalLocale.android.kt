package org.cf0x.spicecompose.ui.i18n

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalLocale {
    @Composable
    actual infix fun provides(value: String): ProvidedValue<*> {
        val locale = if (value.contains("-r")) {
            val parts = value.split("-r")
            Locale(parts[0], parts[1])
        } else {
            Locale(value)
        }
        
        Locale.setDefault(locale)
        
        val configuration = Configuration(LocalConfiguration.current)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        
        val context = LocalContext.current
        val resources = context.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        
        return LocalConfiguration provides configuration
    }
}
