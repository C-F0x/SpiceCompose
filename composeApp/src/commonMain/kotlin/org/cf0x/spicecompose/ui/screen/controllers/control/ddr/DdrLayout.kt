package org.cf0x.spicecompose.ui.screen.controllers.control.ddr

import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.ui.screen.controllers.ControllerLayout

val ddrLayout = ControllerLayout(
    gameModels   = listOf("JDX", "KDX", "MDX", "TDX"),
    name         = "DanceDanceRevolution",
    maxPlayerNum = 2,
    subViews     = listOf("P1 SP", "P2 SP", "DP", "Menu"),
    content      = { conn, i -> DdrController(conn, i) },
)
