package org.cf0x.spicecompose.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.component.navigation.BottomBar
import org.cf0x.spicecompose.ui.component.navigation.SideRail
import org.cf0x.spicecompose.ui.navigation.*
import org.cf0x.spicecompose.ui.screen.status.StatusScreen
import org.cf0x.spicecompose.ui.screen.tools.ToolsScreen
import org.cf0x.spicecompose.ui.screen.utils.UtilsScreen
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBar
import top.yukonga.miuix.kmp.basic.Scaffold

@Composable
fun MainScreen(
    navLayoutMode:         NavLayoutMode,
    onNavLayoutModeChange: (NavLayoutMode) -> Unit,
    settingsContent:       @Composable () -> Unit,
) {
    val pagerState     = rememberPagerState(pageCount = { Destination.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)

    CompositionLocalProvider(LocalMainPagerState provides mainPagerState) {
        val isFloating     = LocalFloatingBottomBar.current
        // Disable swipe while a sub-page is open
        val inSubPage      = LocalInSubPage.current.value

        LaunchedEffect(pagerState.settledPage) { mainPagerState.syncPage() }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = when (navLayoutMode) {
                NavLayoutMode.Auto      -> maxWidth >= 600.dp
                NavLayoutMode.SideRail  -> true
                NavLayoutMode.BottomBar -> false
            }

            when {
                useRail -> {
                    Scaffold { innerPadding ->
                        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            SideRail()
                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                PageContent(
                                    pagerState      = pagerState,
                                    bottomPadding   = 0.dp,
                                    userScrollEnabled = !inSubPage,
                                    settingsContent = settingsContent,
                                )
                            }
                        }
                    }
                }

                isFloating -> {
                    // Floating bar: overlay approach — content goes edge-to-edge
                    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    Box(modifier = Modifier.fillMaxSize()) {
                        PageContent(
                            pagerState        = pagerState,
                            // Reserve enough space so content isn't hidden behind the floating pill
                            bottomPadding     = 56.dp + 12.dp + 12.dp + navBarHeight,
                            userScrollEnabled = !inSubPage,
                            settingsContent   = settingsContent,
                        )
                        BottomBar(modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }

                else -> {
                    Scaffold(
                        bottomBar = { BottomBar() },
                    ) { innerPadding ->
                        PageContent(
                            pagerState        = pagerState,
                            bottomPadding     = innerPadding.calculateBottomPadding(),
                            userScrollEnabled = !inSubPage,
                            settingsContent   = settingsContent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageContent(
    pagerState:        androidx.compose.foundation.pager.PagerState,
    bottomPadding:     Dp,
    userScrollEnabled: Boolean,
    settingsContent:   @Composable () -> Unit,
) {
    HorizontalPager(
        state             = pagerState,
        userScrollEnabled = userScrollEnabled,
        modifier          = Modifier.fillMaxSize().padding(bottom = bottomPadding),
    ) { page ->
        when (page) {
            0    -> StatusScreen()
            1    -> ToolsScreen()
            2    -> UtilsScreen()
            else -> settingsContent()
        }
    }
}
