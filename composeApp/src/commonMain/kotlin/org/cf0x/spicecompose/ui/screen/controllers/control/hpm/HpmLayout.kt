package org.cf0x.spicecompose.ui.screen.controllers.control.hpm

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val hpmLayout = ControllerLayout(
    gameModels   = listOf("JMP"),
    name         = "HELLO! POP'N MUSIC",
    maxPlayerNum = 2,
    subViews     = listOf("P1", "P2"),
    content      = { conn, i -> HpmController(conn, i) },
)
