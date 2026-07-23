package org.cf0x.spicecompose.ui.screen.controllers.control.iidx

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val iidxLayout = ControllerLayout(
    gameModels   = listOf("JDZ", "KDZ", "LDJ", "TDJ"),
    name         = "beatmania IIDX",
    maxPlayerNum = 2,
    subViews     = listOf("P1 7K+1P", "P2 7K+1P", "DP 7K+1P"),
    content      = { conn, i -> IidxController(conn, i) },
)
