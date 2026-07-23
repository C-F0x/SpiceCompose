package org.cf0x.spicecompose.ui.screen.controllers

import androidx.compose.runtime.Composable
import org.cf0x.spicecompose.network.ConnectionManager

/**
 * Declares a preset controller for one game series.
 *
 * Each game-type folder under [control] provides one instance of this
 * together with its button map and Composable body.
 *
 * [AllLayouts] collects every instance; [ControllerScreen] picks the
 * matching one at runtime based on the game model reported by Spice2x.
 */
data class ControllerLayout(
    /** Spice2x model codes this layout supports (e.g. ["LDJ","KDZ","JDZ","TDJ"]). */
    val gameModels: List<String>,
    /** Human-readable game name shown in the subview bar. */
    val name: String,
    /** Maximum number of simultaneous players (affects P1/P2 toggle in tools). */
    val maxPlayerNum: Int = 1,
    /** Ordered list of subview labels (empty list = single view, no bar). */
    val subViews: List<String> = emptyList(),
    /** Composable that renders the controller body for [subViewIndex]. */
    val content: @Composable (ConnectionManager, subViewIndex: Int) -> Unit,
)
