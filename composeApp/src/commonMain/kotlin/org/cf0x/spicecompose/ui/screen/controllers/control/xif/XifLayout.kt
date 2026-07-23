package org.cf0x.spicecompose.ui.screen.controllers.control.xif

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val xifLayout = ControllerLayout(
    gameModels = listOf("XIF"),
    name       = "Polaris Chord",
    content    = { conn, i -> XifController(conn, i) },
)
