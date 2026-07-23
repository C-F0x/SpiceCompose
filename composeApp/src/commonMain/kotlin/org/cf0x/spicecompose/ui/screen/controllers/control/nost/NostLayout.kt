package org.cf0x.spicecompose.ui.screen.controllers.control.nost

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val nostLayout = ControllerLayout(
    gameModels = listOf("PAN"),
    name       = "NOSTALGIA",
    content    = { conn, i -> NostController(conn, i) },
)
