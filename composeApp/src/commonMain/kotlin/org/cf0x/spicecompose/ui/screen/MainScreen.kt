package org.cf0x.spicecompose.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.component.navigation.BottomBar
import org.cf0x.spicecompose.ui.component.navigation.SideRail
import org.cf0x.spicecompose.ui.navigation.*
import org.cf0x.spicecompose.ui.screen.status.StatusScreen
import org.cf0x.spicecompose.ui.screen.tools.ToolsScreen
import org.cf0x.spicecompose.ui.screen.utils.UtilsScreen
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBar
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBarBlur
import org.cf0x.spicecompose.ui.theme.LocalBottomBarPadding
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
        val inSubPage      = LocalInSubPage.current.value
        val fullscreen     = LocalFullscreenMode.current

        LaunchedEffect(inSubPage) {
            if (!inSubPage) {
                fullscreen.value = false
            }
        }

        LaunchedEffect(pagerState.settledPage) {
            if (mainPagerState.lastPage != pagerState.settledPage) {
                mainPagerState.emitReset(mainPagerState.lastPage)
                mainPagerState.lastPage = pagerState.settledPage
                fullscreen.value = false
            }
            mainPagerState.syncPage()
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowSize = getWindowSize(maxWidth)
            val useRail = when (navLayoutMode) {
                NavLayoutMode.Auto      -> maxWidth >= 600.dp
                NavLayoutMode.SideRail  -> true
                NavLayoutMode.BottomBar -> false
            }

            // Calculate bottom padding required to avoid the bar
            val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val dynamicBottomPadding = when {
                fullscreen.value -> 0.dp
                useRail -> 0.dp
                isFloating -> 56.dp + 12.dp + 12.dp + navBarHeight
                else -> navBarHeight + 56.dp // Standard bottom bar height
            }

            CompositionLocalProvider(
                LocalWindowSize provides windowSize,
                LocalBottomBarPadding provides dynamicBottomPadding
            ) {
                Scaffold(
                    bottomBar = {
                        if (!fullscreen.value && !useRail && !isFloating) {
                            BottomBar()
                        }
                    }
                ) { innerPadding ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (!fullscreen.value && useRail) {
                            SideRail()
                        }
                        
                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            // PageContent now always fillMaxSize to let background flow through
                            PageContent(
                                pagerState = pagerState,
                                userScrollEnabled = !inSubPage,
                                settingsContent = settingsContent,
                            )
                            
                            // Floating BottomBar on top of content
                            if (!fullscreen.value && !useRail && isFloating) {
                                BottomBar(modifier = Modifier.align(Alignment.BottomCenter))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageContent(
    pagerState:        androidx.compose.foundation.pager.PagerState,
    userScrollEnabled: Boolean,
    settingsContent:   @Composable () -> Unit,
) {
    HorizontalPager(
        state             = pagerState,
        userScrollEnabled = userScrollEnabled,
        modifier          = Modifier.fillMaxSize(), // Removed bottom padding here!
    ) { page ->
        when (page) {
            0    -> StatusScreen()
            1    -> ToolsScreen()
            2    -> UtilsScreen()
            else -> settingsContent()
        }
    }
}
