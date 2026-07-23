package org.cf0x.spicecompose.ui.screen.controllers.control.we

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val weLayout = ControllerLayout(
    gameModels = listOf("KCK", "NCK"),
    name       = "Winning Eleven",
    content    = { conn, i -> WeController(conn, i) },
)
