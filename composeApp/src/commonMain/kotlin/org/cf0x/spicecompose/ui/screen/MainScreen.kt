package org.cf0x.spicecompose.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.navigation.*
import org.cf0x.spicecompose.ui.theme.LocalFloatingBottomBar
import top.yukonga.miuix.kmp.basic.Scaffold

@Composable
fun MainScreen(
    navLayoutMode: NavLayoutMode,
    onNavLayoutModeChange: (NavLayoutMode) -> Unit,
    settingsContent: @Composable () -> Unit,
) {
    val pagerState     = androidx.compose.foundation.pager.rememberPagerState(pageCount = { Destination.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)
    val isFloating     = LocalFloatingBottomBar.current

    LaunchedEffect(pagerState.settledPage) { mainPagerState.syncPage() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = when (navLayoutMode) {
            NavLayoutMode.Auto      -> maxWidth >= 600.dp
            NavLayoutMode.SideRail  -> true
            NavLayoutMode.BottomBar -> false
        }

        if (useRail) {
            // ── Side rail layout ─────────────────────────────────────────────
            Scaffold { innerPadding ->
                Row(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                ) {
                    SpiceSideRail(
                        selectedIndex  = mainPagerState.selectedPage,
                        onItemSelected = { mainPagerState.animateToPage(it) },
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        PageContent(pagerState, 0.dp, settingsContent)
                    }
                }
            }
        } else if (isFloating) {
            // ── Floating bottom bar: overlay approach ─────────────────────────
            // No Scaffold bottomBar slot – we overlay the bar manually so it
            // truly floats and content isn't pushed by the bar's size.
            Box(modifier = Modifier.fillMaxSize()) {
                PageContent(
                    pagerState      = pagerState,
                    // Reserve space under content so it doesn't hide behind bar:
                    // approx. 56dp (bar) + 12dp top-margin + 12dp bottom-margin + nav-bar inset
                    bottomPadding   = 80.dp + WindowInsets.navigationBars
                        .asPaddingValues().calculateBottomPadding(),
                    settingsContent = settingsContent,
                )
                // Floating bar sits at the bottom, above content
                SpiceBottomBar(
                    selectedIndex  = mainPagerState.selectedPage,
                    onItemSelected = { mainPagerState.animateToPage(it) },
                    modifier       = Modifier.align(Alignment.BottomCenter),
                )
            }
        } else {
            // ── Standard bottom bar via Scaffold ─────────────────────────────
            Scaffold(
                bottomBar = {
                    SpiceBottomBar(
                        selectedIndex  = mainPagerState.selectedPage,
                        onItemSelected = { mainPagerState.animateToPage(it) },
                    )
                },
            ) { innerPadding ->
                PageContent(
                    pagerState      = pagerState,
                    bottomPadding   = innerPadding.calculateBottomPadding(),
                    settingsContent = settingsContent,
                )
            }
        }
    }
}

@Composable
private fun PageContent(
    pagerState:      androidx.compose.foundation.pager.PagerState,
    bottomPadding:   Dp,
    settingsContent: @Composable () -> Unit,
) {
    androidx.compose.foundation.pager.HorizontalPager(
        state    = pagerState,
        modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding),
    ) { page ->
        when (page) {
            0 -> StatusPlaceholder()
            1 -> ToolsPlaceholder()
            2 -> UtilsPlaceholder()
            3 -> settingsContent()
        }
    }
}
