package org.cf0x.spicecompose.ui.screen.tools

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.ui.screen.feature.*

private const val ROUTE_MAIN    = "main"
private const val ROUTE_BUTTONS = "buttons"
private const val ROUTE_ANALOGS = "analogs"
private const val ROUTE_LIGHTS  = "lights"
private const val ROUTE_COINS   = "coins"
private const val ROUTE_KEYPAD  = "keypad"

@Composable
fun ToolsScreen() {
    var route by rememberSaveable { mutableStateOf(ROUTE_MAIN) }
    val mainState = LocalMainPagerState.current

    // Handle reset events from BottomBar
    LaunchedEffect(mainState) {
        mainState.resetEvents
            .filter { it == Destination.Tools.index }
            .collect { route = ROUTE_MAIN }
    }

    val inSubPage = LocalInSubPage.current
    SideEffect { inSubPage.value = route != ROUTE_MAIN }

    SpiceBackHandler(enabled = route != ROUTE_MAIN) { route = ROUTE_MAIN }

    when (route) {
        ROUTE_BUTTONS -> ButtonsScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_ANALOGS -> AnalogsScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_LIGHTS  -> LightsScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_COINS   -> CoinsScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_KEYPAD  -> KeypadScreen(onBack = { route = ROUTE_MAIN })
        else -> {
            val actions = ToolsScreenActions(
                onOpenButtons = { route = ROUTE_BUTTONS },
                onOpenAnalogs = { route = ROUTE_ANALOGS },
                onOpenLights  = { route = ROUTE_LIGHTS },
                onOpenCoins   = { route = ROUTE_COINS },
                onOpenKeypad  = { route = ROUTE_KEYPAD },
            )
            when (LocalUiMode.current) {
                UiMode.Miuix    -> ToolsPagerMiuix(actions)
                UiMode.Material -> ToolsPagerMaterial(actions)
            }
        }
    }
}

data class ToolsScreenActions(
    val onOpenButtons: () -> Unit,
    val onOpenAnalogs: () -> Unit,
    val onOpenLights:  () -> Unit,
    val onOpenCoins:   () -> Unit,
    val onOpenKeypad:  () -> Unit,
)
