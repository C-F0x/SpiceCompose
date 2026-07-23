package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState

@Composable
fun SideRailMaterial(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainState = LocalMainPagerState.current

    NavigationRail(
        modifier = modifier,
    ) {
        Destination.all.forEach { dest ->
            NavigationRailItem(
                selected = mainState.selectedPage == dest.index,
                onClick  = { maybeVibrate(20); mainState.animateToPage(dest.index) },
                icon     = { Icon(if (mainState.selectedPage == dest.index) dest.materialFilled else dest.materialOutlined, contentDescription = dest.label) },
                label    = if (expanded) {{ Text(dest.label) }} else null,
                alwaysShowLabel = expanded,
            )
        }
    }
}
