package org.cf0x.spicecompose.ui.screen.controllers

import org.cf0x.spicecompose.ui.screen.controllers.control.bbc.bbcLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.ddr.ddrLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.drs.drsLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.ftt.fttLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.hpm.hpmLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.iidx.iidxLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.jb.jbLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.lp.lpLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.nost.nostLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.popn.popnLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.rf3d.rf3dLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.sdvx.sdvxLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.we.weLayout
import org.cf0x.spicecompose.ui.screen.controllers.control.xif.xifLayout

/** All known game-controller layouts. Add new games here. */
val allLayouts = listOf(
    bbcLayout,
    ddrLayout,
    drsLayout,
    fttLayout,
    hpmLayout,
    iidxLayout,
    jbLayout,
    lpLayout,
    nostLayout,
    popnLayout,
    rf3dLayout,
    sdvxLayout,
    weLayout,
    xifLayout,
)

/** Fast lookup: Spice2x model code → [ControllerLayout]. */
val layoutByModel: Map<String, ControllerLayout> =
    allLayouts.flatMap { layout -> layout.gameModels.map { it to layout } }.toMap()
