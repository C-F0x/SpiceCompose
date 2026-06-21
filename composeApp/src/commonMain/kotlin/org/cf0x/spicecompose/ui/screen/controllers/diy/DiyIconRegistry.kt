package org.cf0x.spicecompose.ui.screen.controllers.diy

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lookup table for icon name → ImageVector.
 * Used by [DiyRenderer] and the icon picker.
 */
object DiyIconRegistry {
    val all: List<Pair<String, ImageVector>> = listOf(
        "Gamepad" to Icons.Rounded.Gamepad,
        "ArrowUpward" to Icons.Rounded.ArrowUpward,
        "ArrowDownward" to Icons.Rounded.ArrowDownward,
        "KeyboardArrowUp" to Icons.Rounded.KeyboardArrowUp,
        "KeyboardArrowDown" to Icons.Rounded.KeyboardArrowDown,
        "KeyboardArrowLeft" to Icons.Rounded.KeyboardArrowLeft,
        "KeyboardArrowRight" to Icons.Rounded.KeyboardArrowRight,
        "Refresh" to Icons.Rounded.Refresh,
        "Search" to Icons.Rounded.Search,
        "Star" to Icons.Rounded.Star,
        "Close" to Icons.Rounded.Close,
        "Check" to Icons.Rounded.Check,
        "Add" to Icons.Rounded.Add,
        "Remove" to Icons.Rounded.Remove,
        "Settings" to Icons.Rounded.Settings,
        "PlayArrow" to Icons.Rounded.PlayArrow,
        "Pause" to Icons.Rounded.Pause,
        "SkipNext" to Icons.Rounded.SkipNext,
        "SkipPrevious" to Icons.Rounded.SkipPrevious,
    )

    fun get(name: String): ImageVector? = all.find { it.first == name }?.second
    val names: List<String> get() = all.map { it.first }
}
