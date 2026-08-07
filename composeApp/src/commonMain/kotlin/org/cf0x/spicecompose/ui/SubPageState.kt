package org.cf0x.spicecompose.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Shared sub-page state for the main pager.
 *
 * MainScreen reads [active] to hide the bottom bar / side rail and disable pager
 * swiping while any page is showing a sub-page (SubScreen, Patches, Settings…
 * theme/about, server list, …).
 *
 * Multiple pager pages stay composed at the same time (visible + prefetch), so a
 * single boolean written by each page would race and overwrite each other — hence
 * the depth counter: [enter] on sub-page open, [exit] on close/dispose. [active]
 * is true while depth > 0, i.e. any page has a sub-page open.
 */
class SubPageState {
    private var depth = 0

    var active by mutableStateOf(false)
        private set

    fun enter() {
        depth++
        active = true
    }

    fun exit() {
        depth = (depth - 1).coerceAtLeast(0)
        if (depth == 0) active = false
    }
}

val LocalInSubPage = compositionLocalOf { SubPageState() }
