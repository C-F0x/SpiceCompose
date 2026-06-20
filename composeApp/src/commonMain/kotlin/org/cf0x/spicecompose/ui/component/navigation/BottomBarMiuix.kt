package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBar
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBarBlur
import org.cf0x.spicecompose.ui.util.BlurredBar
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBarMiuix(
    blurBackdrop: LayerBackdrop?,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
) {
    val mainState = LocalMainPagerState.current
    val enableFloatingBottomBar = LocalFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalFloatingBottomBarBlur.current

    if (!enableFloatingBottomBar) {
        // Box receives the alignment modifier so it positions correctly as a
        // direct child of the parent Box. BlurredBar creates its own Box which
        // would break the align() scope otherwise.
        Box(modifier = modifier) {
            BlurredBar(blurBackdrop) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (blurBackdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface,
                ) {
                    Destination.all.forEach { dest ->
                        NavigationBarItem(
                            modifier = Modifier.weight(1f),
                            icon = dest.icon,
                            label = dest.label,
                            selected = mainState.selectedPage == dest.index,
                            onClick = { maybeVibrate(20); mainState.animateToPage(dest.index) }
                        )
                    }
                }
            }
        }
    } else {
        FloatingBottomBar(
            modifier = modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            selectedIndex = { mainState.selectedPage },
            onSelected = { maybeVibrate(20); mainState.animateToPage(it) },
            tabsCount = Destination.PAGE_COUNT,
            isBlurEnabled = enableFloatingBottomBarBlur,
            parentBackdrop = backdrop,
        ) {
            Destination.all.forEach { dest ->
                FloatingBottomBarItem(
                    onClick = { maybeVibrate(20); mainState.animateToPage(dest.index) },
                ) {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dest.label,
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
