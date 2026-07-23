package org.cf0x.spicecompose.ui.screen.controllers.control.lp

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val lpLayout = ControllerLayout(
    gameModels = listOf("KLP"),
    name       = "LOVEPLUS",
    content    = { conn, i -> LpController(conn, i) },
)
