package org.cf0x.spicecompose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpiceCompose",
    ) {
        App()
    }
}