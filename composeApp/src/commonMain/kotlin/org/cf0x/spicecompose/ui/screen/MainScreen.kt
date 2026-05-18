package org.cf0x.spicecompose.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.navigation.SpiceBottomBar
import org.cf0x.spicecompose.ui.navigation.SpiceSideRail
import org.cf0x.spicecompose.ui.navigation.rememberMainPagerState
import top.yukonga.miuix.kmp.basic.Scaffold

@Composable
fun MainScreen(
    navLayoutMode: NavLayoutMode,
    onNavLayoutModeChange: (NavLayoutMode) -> Unit,
) {
    val pagerState     = rememberPagerState(pageCount = { Destination.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)

    LaunchedEffect(pagerState.settledPage) { mainPagerState.syncPage() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = when (navLayoutMode) {
            NavLayoutMode.Auto      -> maxWidth >= 600.dp
            NavLayoutMode.SideRail  -> true
            NavLayoutMode.BottomBar -> false
        }

        if (useRail) {
            Scaffold { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    SpiceSideRail(
                        selectedIndex  = mainPagerState.selectedPage,
                        onItemSelected = { mainPagerState.animateToPage(it) },
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        PageContent(pagerState = pagerState, bottomPadding = 0.dp)
                    }
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    SpiceBottomBar(
                        selectedIndex  = mainPagerState.selectedPage,
                        onItemSelected = { mainPagerState.animateToPage(it) },
                    )
                },
            ) { innerPadding ->
                PageContent(
                    pagerState    = pagerState,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }
        }
    }
}

@Composable
private fun PageContent(
    pagerState: androidx.compose.foundation.pager.PagerState,
    bottomPadding: Dp,
) {
    HorizontalPager(
        state    = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
    ) { page ->
        when (page) {
            0 -> StatusPlaceholder()
            1 -> ToolsPlaceholder()
            2 -> UtilsPlaceholder()
            3 -> SettingsPlaceholder()
        }
    }
}
