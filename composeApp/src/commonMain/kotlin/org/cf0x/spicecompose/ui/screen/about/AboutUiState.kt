package org.cf0x.spicecompose.ui.screen.about

import androidx.compose.runtime.Immutable
import org.cf0x.spicecompose.util.APP_VERSION

const val GITHUB_URL = "https://github.com/C-F0x/SpiceCompose"

@Immutable
data class AboutUiState(
    val appName:     String = "SpiceCompose",
    val versionName: String = APP_VERSION,
)

@Immutable
data class AboutScreenActions(
    val onBack:     () -> Unit,
    val onOpenLink: (String) -> Unit,
    val onOpenFaq:  () -> Unit = {},
)
