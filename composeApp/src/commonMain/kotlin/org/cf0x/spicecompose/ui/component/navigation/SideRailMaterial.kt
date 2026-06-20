package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState

@Composable
fun SideRailMaterial(
    modifier: Modifier = Modifier,
) {
    val mainState = LocalMainPagerState.current

    NavigationRail(modifier = modifier) {
        Destination.all.forEach { dest ->
            NavigationRailItem(
                selected = mainState.selectedPage == dest.index,
                onClick  = { maybeVibrate(20); mainState.animateToPage(dest.index) },
                icon     = { Icon(dest.icon, contentDescription = dest.label) },
                label    = { Text(dest.label) },
            )
        }
    }
}
