package org.cf0x.spicecompose.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Sidebar
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.icon.extended.Settings

sealed class Destination(
    val index: Int,
    val label: String,
    val materialOutlined: ImageVector,
    val materialFilled: ImageVector,
    val miuixIcon: @Composable () -> ImageVector,
) {
    data object Status   : Destination(0, "Status",   Icons.Outlined.Dashboard,    Icons.Filled.Dashboard,    { MiuixIcons.Sidebar })
    data object Tools    : Destination(1, "Tools",    Icons.Outlined.Construction, Icons.Filled.Construction, { MiuixIcons.Tune })
    data object Settings : Destination(2, "Settings", Icons.Outlined.Settings,     Icons.Filled.Settings,     { MiuixIcons.Settings })

    companion object {
        val all        = listOf(Status, Tools, Settings)
        val PAGE_COUNT = all.size
    }
}
