@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package org.cf0x.spicecompose.ui.i18n

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ScriptQualifier
import org.jetbrains.compose.resources.ThemeQualifier

@Composable
actual fun ProvideAppLocale(value: String, content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val density = LocalDensity.current
    val environment = remember(value, isDarkTheme, density.density) {
        val locale = value.toIosResourceLocale()
        ResourceEnvironment(
            language = LanguageQualifier(locale.language),
            script = ScriptQualifier(locale.script),
            region = RegionQualifier(locale.region),
            theme = ThemeQualifier.selectByValue(isDarkTheme),
            density = DensityQualifier.selectByDensity(density.density),
        )
    }
    val composeEnvironment = remember(environment) {
        object : ComposeEnvironment {
            @Composable
            override fun rememberEnvironment(): ResourceEnvironment = environment
        }
    }

    CompositionLocalProvider(
        LocalLocale provides value,
        LocalComposeEnvironment provides composeEnvironment,
        content = content,
    )
}

private data class IosResourceLocale(
    val language: String,
    val script: String,
    val region: String,
)

private fun String.toIosResourceLocale(): IosResourceLocale {
    val parts = split("-r", limit = 2)
    val language = parts.firstOrNull().orEmpty().ifBlank { "en" }
    val region = parts.getOrNull(1).orEmpty()
    return IosResourceLocale(language = language, script = "", region = region)
}
