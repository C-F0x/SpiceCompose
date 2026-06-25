package org.cf0x.spicecompose.ui.i18n

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android 13+ allows per-app language overrides via system Settings.
 * Uses LocaleManager.getApplicationLocales() to detect an active override.
 */
@Composable
actual fun isSystemLocaleOverridden(): Boolean {
    if (Build.VERSION.SDK_INT < 33) return false
    val context = LocalContext.current
    return try {
        val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
        val appLocales = localeManager?.applicationLocales
        // Non-empty LocaleList means the user explicitly set a language for this app
        appLocales != null && !appLocales.isEmpty
    } catch (_: Exception) {
        false
    }
}
