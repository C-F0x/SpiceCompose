package org.cf0x.spicecompose.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(
    val index: Int,
    val label: String,
    val icon:  ImageVector,
) {
    data object Status   : Destination(0, "Status",   Icons.Outlined.Dashboard)
    data object Tools    : Destination(1, "Tools",    Icons.Outlined.Construction)
    data object Settings : Destination(2, "Settings", Icons.Outlined.Settings)

    companion object {
        val all        = listOf(Status, Tools, Settings)
        val PAGE_COUNT = all.size
    }
}
