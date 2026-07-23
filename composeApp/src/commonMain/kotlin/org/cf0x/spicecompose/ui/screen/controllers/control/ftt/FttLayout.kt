package org.cf0x.spicecompose.ui.screen.controllers.control.ftt

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val fttLayout = ControllerLayout(
    gameModels = listOf("MMD"),
    name       = "Future TomTom",
    content    = { conn, i -> FttController(conn, i) },
)
