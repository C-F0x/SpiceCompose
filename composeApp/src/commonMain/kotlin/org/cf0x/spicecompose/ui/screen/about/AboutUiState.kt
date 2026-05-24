package org.cf0x.spicecompose.ui.screen.about

import androidx.compose.runtime.Immutable

const val GITHUB_URL = "https://github.com/C-F0x/SpiceCompose"

@Immutable
data class AboutUiState(
    val appName:     String = "SpiceCompose",
    val versionName: String = "0.1.0",
)

@Immutable
data class AboutScreenActions(
    val onBack:     () -> Unit,
    val onOpenLink: (String) -> Unit,
)
