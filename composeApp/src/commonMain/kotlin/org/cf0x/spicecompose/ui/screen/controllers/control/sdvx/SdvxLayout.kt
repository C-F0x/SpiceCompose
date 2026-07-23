package org.cf0x.spicecompose.ui.screen.controllers.control.sdvx

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val sdvxLayout = ControllerLayout(
    gameModels = listOf("KFC", "UFC"),
    name       = "SOUND VOLTEX",
    content    = { conn, i -> SdvxController(conn, i) },
)
