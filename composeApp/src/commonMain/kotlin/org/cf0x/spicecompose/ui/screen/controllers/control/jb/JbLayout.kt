package org.cf0x.spicecompose.ui.screen.controllers.control.jb

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val jbLayout = ControllerLayout(
    gameModels = listOf("J44", "K44", "L44"),
    name       = "jubeat",
    content    = { conn, i -> JbController(conn, i) },
)
