package org.cf0x.spicecompose.ui.screen.utils

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.ui.screen.utils.subscreen.SubScreen
import org.cf0x.spicecompose.ui.screen.feature.*

private const val ROUTE_MAIN        = "main"
private const val ROUTE_SUB_SCREEN  = "sub_screen"
private const val ROUTE_PATCHES     = "patches"

@Composable
fun UtilsScreen() {
    var route by rememberSaveable { mutableStateOf(ROUTE_MAIN) }
    val mainState = LocalMainPagerState.current

    // Handle reset events from BottomBar
    LaunchedEffect(mainState) {
        mainState.resetEvents
            .filter { it == Destination.Utils.index }
            .collect { route = ROUTE_MAIN }
    }

    val inSubPage = LocalInSubPage.current
    SideEffect { inSubPage.value = route != ROUTE_MAIN }

    SpiceBackHandler(enabled = route != ROUTE_MAIN) { route = ROUTE_MAIN }

    when (route) {
        ROUTE_SUB_SCREEN -> SubScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_PATCHES    -> PatchesScreen(onBack = { route = ROUTE_MAIN })
        else -> {
            val actions = UtilsScreenActions(
                onOpenSubScreen = { route = ROUTE_SUB_SCREEN },
                onOpenPatches   = { route = ROUTE_PATCHES },
            )
            when (LocalUiMode.current) {
                UiMode.Miuix    -> UtilsPagerMiuix(actions)
                UiMode.Material -> UtilsPagerMaterial(actions)
            }
        }
    }
}

data class UtilsScreenActions(
    val onOpenSubScreen: () -> Unit,
    val onOpenPatches:   () -> Unit,
)
