package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.runtime.Composable
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode

@Composable
fun SubScreen(
    onBack: () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SubScreenMiuix(onBack)
        UiMode.Material -> SubScreenMaterial(onBack)
    }
}
