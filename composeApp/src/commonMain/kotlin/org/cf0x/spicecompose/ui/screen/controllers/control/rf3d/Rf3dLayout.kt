package org.cf0x.spicecompose.ui.screen.controllers.control.rf3d

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val rf3dLayout = ControllerLayout(
    gameModels = listOf("JGT"),
    name       = "ROAD FIGHTERS 3D",
    content    = { conn, i -> Rf3dController(conn, i) },
)
