package org.cf0x.spicecompose.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.runtime.Composable
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode

@Composable
fun FullscreenAction() {
    val fullscreen = LocalFullscreenMode.current
    val uiMode = LocalUiMode.current

    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.IconButton(onClick = { fullscreen.value = !fullscreen.value }) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = if (fullscreen.value) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                contentDescription = "Toggle Fullscreen"
            )
        }
    } else {
        androidx.compose.material3.IconButton(onClick = { fullscreen.value = !fullscreen.value }) {
            androidx.compose.material3.Icon(
                imageVector = if (fullscreen.value) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                contentDescription = "Toggle Fullscreen"
            )
        }
    }
}
