package org.cf0x.spicecompose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.cf0x.spicecompose.ui.DesktopBackDispatcher
import org.jetbrains.compose.resources.painterResource
import spicecompose.composeapp.generated.resources.Res
import spicecompose.composeapp.generated.resources.ic_launcher

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpiceCompose",
        icon = painterResource(Res.drawable.ic_launcher),
        onPreviewKeyEvent = { event ->
            event.type == KeyEventType.KeyUp &&
                event.key == Key.Escape &&
                DesktopBackDispatcher.dispatch()
        },
        state = rememberWindowState(size = DpSize(1024.dp, 768.dp)),
    ) {
        App()
    }
}
