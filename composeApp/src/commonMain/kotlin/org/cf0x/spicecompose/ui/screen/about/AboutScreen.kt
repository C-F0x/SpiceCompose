package org.cf0x.spicecompose.ui.screen.about

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode

@ExperimentalMaterial3Api
@Composable
fun AboutScreen(onBack: () -> Unit, onOpenFaq: () -> Unit = {}) {
    val uriHandler = LocalUriHandler.current
    val state   = AboutUiState()
    val actions = AboutScreenActions(
        onBack     = onBack,
        onOpenLink = uriHandler::openUri,
        onOpenFaq  = onOpenFaq,
    )
    when (LocalUiMode.current) {
        UiMode.Miuix    -> AboutScreenMiuix(state, actions)
        UiMode.Material -> AboutScreenMaterial(state, actions)
    }
}
