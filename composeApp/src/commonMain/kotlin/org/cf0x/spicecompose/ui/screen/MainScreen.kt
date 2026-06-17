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
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.navigation.BottomBar
import org.cf0x.spicecompose.ui.component.navigation.SideRail
import org.cf0x.spicecompose.ui.navigation.*
import org.cf0x.spicecompose.ui.screen.status.StatusScreen
import org.cf0x.spicecompose.ui.screen.tools.ToolsScreen
import org.cf0x.spicecompose.ui.screen.utils.UtilsScreen
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBar
import org.cf0x.spicecompose.ui.theme.LocalBottomBarPadding
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop

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
        val fullscreen     = LocalFullscreenMode.current
        val enableBlur     = LocalEnableBlur.current
        val uiMode         = LocalUiMode.current
        val blurBackdrop   = rememberBlurBackdrop(enableBlur && uiMode == UiMode.Miuix)

        LaunchedEffect(pagerState.settledPage) {
            if (mainPagerState.lastPage != pagerState.settledPage) {
                mainPagerState.emitReset(mainPagerState.lastPage)
                mainPagerState.lastPage = pagerState.settledPage
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
                isFloating -> navBarHeight // pill floats over content — no extra padding needed
                else -> navBarHeight + 56.dp // Standard bottom bar height
            }

            CompositionLocalProvider(
                LocalWindowSize provides windowSize,
                LocalBottomBarPadding provides dynamicBottomPadding
            ) {
                Scaffold { innerPadding ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (!fullscreen.value && useRail) {
                            SideRail()
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            PageContent(
                                pagerState = pagerState,
                                userScrollEnabled = false,
                                settingsContent = settingsContent,
                                blurBackdrop = if (enableBlur && uiMode == UiMode.Miuix) blurBackdrop else null,
                            )
                            // Bottom bar as overlay: floating pill or standard bar
                            if (!fullscreen.value && !useRail) {
                                val barModifier = if (isFloating && uiMode == UiMode.Miuix) {
                                    Modifier.align(Alignment.BottomCenter)
                                } else {
                                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                                }
                                BottomBar(
                                    blurBackdrop = blurBackdrop,
                                    backdrop     = blurBackdrop,
                                    modifier     = barModifier,
                                )
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
    blurBackdrop:      LayerBackdrop? = null,
) {
    val blurModifier = if (blurBackdrop != null) {
        Modifier.fillMaxSize().layerBackdrop(blurBackdrop)
    } else {
        Modifier.fillMaxSize()
    }
    HorizontalPager(
        state             = pagerState,
        userScrollEnabled = userScrollEnabled,
        modifier          = blurModifier,
    ) { page ->
        Box(modifier = Modifier.padding(bottom = LocalBottomBarPadding.current)) {
            when (page) {
                0    -> StatusScreen()
                1    -> ToolsScreen()
                2    -> UtilsScreen()
                else -> settingsContent()
            }
        }
    }
}
