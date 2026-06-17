package org.cf0x.spicecompose.ui.screen.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.ui.screen.feature.*
import org.cf0x.spicecompose.ui.component.TonalCard
import org.cf0x.spicecompose.ui.theme.SpiceTheme

private const val ROUTE_MAIN    = "main"
private const val ROUTE_BUTTONS = "buttons"
private const val ROUTE_ANALOGS = "analogs"
private const val ROUTE_LIGHTS  = "lights"
private const val ROUTE_COINS   = "coins"
private const val ROUTE_KEYPAD  = "keypad"

data class ToolsScreenActions(
    val onOpenButtons: () -> Unit,
    val onOpenAnalogs: () -> Unit,
    val onOpenLights:  () -> Unit,
    val onOpenCoins:   () -> Unit,
    val onOpenKeypad:  () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen() {
    var route by rememberSaveable { mutableStateOf(ROUTE_MAIN) }
    val mainState = LocalMainPagerState.current
    val strings = LocalAppStrings.current

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
                onOpenKeypad  = { route = ROUTE_KEYPAD }
            )
            when (LocalUiMode.current) {
                UiMode.Miuix -> ToolsPagerMiuix(actions)
                UiMode.Material -> {
                    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                    Scaffold(
                        topBar = { TopAppBar(title = { Text(strings.tools) }, scrollBehavior = scrollBehavior) }
                    ) { innerPadding ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                            contentPadding = innerPadding,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item { Spacer(Modifier.height(8.dp)) }
                            val items = listOf(
                                Triple(strings.buttons, strings.buttonsSummary, actions.onOpenButtons),
                                Triple(strings.analogs, strings.analogsSummary, actions.onOpenAnalogs),
                                Triple(strings.lights, strings.lightsSummary, actions.onOpenLights),
                                Triple(strings.coins, strings.coinsSummary, actions.onOpenCoins),
                                Triple(strings.keypadScanner, strings.keypadSummary, actions.onOpenKeypad)
                            )
                            items(items) { (title, summary, onClick) ->
                                Box(Modifier.padding(horizontal = 16.dp)) {
                                    TonalCard(shape = SpiceTheme.containerShape(), onClick = onClick) {
                                        ListItem(
                                            headlineContent = { Text(title) },
                                            supportingContent = { Text(summary) },
                                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                                        )
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}
