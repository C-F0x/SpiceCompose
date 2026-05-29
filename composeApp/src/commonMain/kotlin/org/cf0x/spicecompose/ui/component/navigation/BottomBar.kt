package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix    -> BottomBarMiuix(modifier)
        UiMode.Material -> BottomBarMaterial(modifier)
    }
}
