package org.cf0x.spicecompose.ui.screen.controllers.control.drs

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val drsLayout = ControllerLayout(
    gameModels   = listOf("REC"),
    name         = "DANCERUSH STARDOM",
    maxPlayerNum = 2,
    content      = { conn, i -> DrsController(conn, i) },
)
