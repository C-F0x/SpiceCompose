package org.cf0x.spicecompose.ui.screen.controllers.control.bbc

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val bbcLayout = ControllerLayout(
    gameModels   = listOf("R66"),
    name         = "BishiBashi Channel",
    maxPlayerNum = 4,
    subViews     = listOf("P1", "P2", "P3", "P4"),
    content      = { conn, i -> BbcController(conn, i) },
)
