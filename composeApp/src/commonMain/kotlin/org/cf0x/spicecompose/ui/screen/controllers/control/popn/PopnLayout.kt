package org.cf0x.spicecompose.ui.screen.controllers.control.popn

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val popnLayout = ControllerLayout(
    gameModels = listOf("K39", "L39", "M39"),
    name       = "pop'n music",
    subViews   = listOf("圆形", "方形"),
    content    = { conn, i -> PopnController(conn, i) },
)
