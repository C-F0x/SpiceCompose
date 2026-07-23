package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.platform.maybeVibrate
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem

@Composable
fun SideRailMiuix(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainState = LocalMainPagerState.current

    NavigationRail(modifier = modifier) {
        Column {
            if (expanded) Spacer(Modifier.height(12.dp))
        }
        Destination.all.forEach { dest ->
            NavigationRailItem(
                selected = mainState.selectedPage == dest.index,
                onClick  = { maybeVibrate(20); mainState.animateToPage(dest.index) },
                icon     = dest.miuixIcon(),
                label    = if (expanded) dest.label else "",
            )
        }
    }
}
