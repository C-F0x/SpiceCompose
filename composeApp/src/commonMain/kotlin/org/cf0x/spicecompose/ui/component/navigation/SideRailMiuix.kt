package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem

@Composable
fun SideRailMiuix(
    modifier: Modifier = Modifier,
) {
    val mainState = LocalMainPagerState.current

    NavigationRail(modifier = modifier) {
        Destination.all.forEach { dest ->
            NavigationRailItem(
                selected = mainState.selectedPage == dest.index,
                onClick  = { mainState.animateToPage(dest.index) },
                icon     = dest.icon,
                label    = dest.label,
            )
        }
    }
}
