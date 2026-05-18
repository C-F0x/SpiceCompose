package org.cf0x.spicecompose.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem

@Composable
fun SpiceBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier.fillMaxWidth()) {
        Destination.all.forEach { dest ->
            NavigationBarItem(
                selected = selectedIndex == dest.index,
                onClick  = { onItemSelected(dest.index) },
                icon     = dest.icon,
                label    = dest.label,
            )
        }
    }
}

@Composable
fun SpiceSideRail(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier = modifier) {
        Destination.all.forEach { dest ->
            NavigationRailItem(
                selected = selectedIndex == dest.index,
                onClick  = { onItemSelected(dest.index) },
                icon     = dest.icon,
                label    = dest.label,
            )
        }
    }
}
