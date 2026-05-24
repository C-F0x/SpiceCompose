package org.cf0x.spicecompose.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBar
import top.yukonga.miuix.kmp.basic.*

@Composable
fun SpiceBottomBar(
    selectedIndex:  Int,
    onItemSelected: (Int) -> Unit,
    modifier:       Modifier = Modifier,
) {
    val isFloating = LocalFloatingBottomBar.current

    val barModifier = if (isFloating) {
        modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .clip(RoundedCornerShape(32.dp))
            .fillMaxWidth()
    } else {
        modifier.fillMaxWidth()
    }

    NavigationBar(modifier = barModifier) {
        Destination.all.forEach { dest ->
            NavigationBarItem(
                selected = selectedIndex == dest.index,
                onClick  = { onItemSelected(dest.index) },
                icon     = { Icon(imageVector = dest.icon, contentDescription = dest.label) },
                label    = { Text(text = dest.label) },
            )
        }
    }
}

@Composable
fun SpiceSideRail(
    selectedIndex:  Int,
    onItemSelected: (Int) -> Unit,
    modifier:       Modifier = Modifier,
) {
    NavigationRail(modifier = modifier) {
        Destination.all.forEach { dest ->
            NavigationRailItem(
                selected = selectedIndex == dest.index,
                onClick  = { onItemSelected(dest.index) },
                icon     = { Icon(imageVector = dest.icon, contentDescription = dest.label) },
                label    = { Text(text = dest.label) },
            )
        }
    }
}
