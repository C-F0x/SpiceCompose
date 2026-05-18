package org.cf0x.spicecompose

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpiceCompose",
        state = rememberWindowState(size = DpSize(1024.dp, 768.dp)),
    ) {
        App()
    }
}
