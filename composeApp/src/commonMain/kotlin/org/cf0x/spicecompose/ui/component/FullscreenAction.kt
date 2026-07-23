package org.cf0x.spicecompose.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings

@Composable
fun FullscreenAction(
    modifier: Modifier = Modifier,
) {
    val fullscreen = LocalFullscreenMode.current
    val uiMode = LocalUiMode.current
    val strings = LocalAppStrings.current

    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.IconButton(
            onClick = { fullscreen.value = !fullscreen.value },
            modifier = modifier.size(35.dp),
        ) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = if (fullscreen.value) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                contentDescription = strings.toggleFullscreen
            )
        }
    } else {
        androidx.compose.material3.IconButton(
            onClick = { fullscreen.value = !fullscreen.value },
            modifier = modifier,
        ) {
            androidx.compose.material3.Icon(
                imageVector = if (fullscreen.value) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                contentDescription = strings.toggleFullscreen
            )
        }
    }
}
