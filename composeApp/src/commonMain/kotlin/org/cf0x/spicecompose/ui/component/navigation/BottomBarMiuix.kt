package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBar
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBarBlur
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBarMiuix(
    modifier: Modifier = Modifier,
) {
    val mainState   = LocalMainPagerState.current
    val isFloating  = LocalFloatingBottomBar.current
    val blurEnabled = LocalFloatingBottomBarBlur.current

    if (isFloating) {
        FloatingBottomBar(
            modifier = modifier
                .padding(bottom = 12.dp), // MainScreen handles the safe area padding now
            selectedIndex = { mainState.selectedPage },
            onSelected    = { mainState.animateToPage(it) },
            tabsCount     = Destination.PAGE_COUNT,
            isBlurEnabled = blurEnabled,
        ) {
            Destination.all.forEach { dest ->
                FloatingBottomBarItem(
                    onClick = { mainState.animateToPage(dest.index) }
                ) {
                    Icon(
                        imageVector        = dest.icon,
                        contentDescription = dest.label,
                        tint               = MiuixTheme.colorScheme.onSurface
                    )
                    Text(
                        text     = dest.label,
                        fontSize = 11.sp,
                        color    = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    } else {
        NavigationBar(modifier = modifier.fillMaxWidth()) {
            Destination.all.forEach { dest ->
                NavigationBarItem(
                    selected = mainState.selectedPage == dest.index,
                    onClick  = { mainState.animateToPage(dest.index) },
                    icon     = dest.icon,
                    label    = dest.label,
                )
            }
        }
    }
}
