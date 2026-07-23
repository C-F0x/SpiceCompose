package org.cf0x.spicecompose.ui.screen.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.component.TonalCard
import org.cf0x.spicecompose.ui.component.WideContentBox
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.LocalDevMode
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState
import org.cf0x.spicecompose.ui.screen.controllers.ControllerScreen
import org.cf0x.spicecompose.ui.screen.controllers.diy.DiyScreen
import org.cf0x.spicecompose.ui.screen.feature.*
import org.cf0x.spicecompose.ui.theme.SpiceTheme

private const val ROUTE_MAIN       = "main"
private const val ROUTE_BUTTONS    = "buttons"
private const val ROUTE_ANALOGS    = "analogs"
private const val ROUTE_LIGHTS     = "lights"
private const val ROUTE_CABINET    = "cabinet"
private const val ROUTE_SUB        = "sub_screen"
private const val ROUTE_PATCHES    = "patches"
private const val ROUTE_CONTROLLER = "controller"
private const val ROUTE_LCD       = "lcd_info"
private const val ROUTE_RESIZE    = "resize"
private const val ROUTE_DIY       = "diy"

data class ToolsScreenActions(
    val onOpenButtons: () -> Unit,
    val onOpenAnalogs: () -> Unit,
    val onOpenLights:  () -> Unit,
    val onOpenCabinetUtility: () -> Unit,
    val onOpenSubScreen: () -> Unit,
    val onOpenPatches:   () -> Unit,
    val onOpenController: () -> Unit,
    val onOpenLcd:   () -> Unit = {},
    val onOpenResize: () -> Unit = {},
    val onOpenDiy:   () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen() {
    var route by rememberSaveable { mutableStateOf(ROUTE_MAIN) }
    val mainState = LocalMainPagerState.current
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current

    LaunchedEffect(mainState) {
        mainState.resetEvents
            .filter { it == Destination.Tools.index }
            .collect { route = ROUTE_MAIN }
    }

    val inSubPage = LocalInSubPage.current
    inSubPage.value = route != ROUTE_MAIN

    SpiceBackHandler(enabled = route != ROUTE_MAIN) { route = ROUTE_MAIN }

    AnimatedContent(
        targetState = route,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { currentRoute ->
        when (currentRoute) {
            ROUTE_BUTTONS    -> ButtonsScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_ANALOGS    -> AnalogsScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_LIGHTS     -> LightsScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_CABINET    -> CabinetUtilityScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_SUB        -> org.cf0x.spicecompose.ui.screen.utils.subscreen.SubScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_PATCHES    -> PatchesScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_CONTROLLER -> ControllerScreen(
                connectionManager = connectionManager,
                onBack = { route = ROUTE_MAIN },
            )
            ROUTE_LCD    -> LcdInfoScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_RESIZE -> ResizeScreen(onBack = { route = ROUTE_MAIN })
            ROUTE_DIY    -> DiyScreen(
                connectionManager = connectionManager,
                onBack = { route = ROUTE_MAIN },
            )
            else -> {
                val actions = ToolsScreenActions(
                    onOpenButtons    = { route = ROUTE_BUTTONS },
                    onOpenAnalogs    = { route = ROUTE_ANALOGS },
                    onOpenLights     = { route = ROUTE_LIGHTS },
                    onOpenCabinetUtility = { route = ROUTE_CABINET },
                    onOpenSubScreen  = { route = ROUTE_SUB },
                    onOpenPatches    = { route = ROUTE_PATCHES },
                    onOpenController = { route = ROUTE_CONTROLLER },
                    onOpenLcd       = { route = ROUTE_LCD },
                    onOpenResize    = { route = ROUTE_RESIZE },
                    onOpenDiy       = { route = ROUTE_DIY },
                )
                when (LocalUiMode.current) {
                    UiMode.Miuix    -> ToolsPagerMiuix(actions)
                    UiMode.Material -> {
                        val devMode = LocalDevMode.current
                        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                        Scaffold(
                            topBar = {
                                AdaptiveTopAppBar(
                                    title = { Text(strings.tools) },
                                    scrollBehavior = scrollBehavior
                                )
                            }
                        ) { innerPadding ->
                            WideContentBox { LazyColumn(
                                modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { Spacer(Modifier.height(12.dp)) }

                                // ── Group: Buttons / Analogs / Lights ──
                                item {
                                    val group1 = listOf(
                                        Triple(strings.buttons, strings.buttonsSummary, { maybeVibrate(15); actions.onOpenButtons() }),
                                        Triple(strings.analogs, strings.analogsSummary, { maybeVibrate(15); actions.onOpenAnalogs() }),
                                        Triple(strings.lights, strings.lightsSummary, { maybeVibrate(15); actions.onOpenLights() }),
                                    )
                                    group1.forEach { (title, summary, onClick) ->
                                        Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                                            TonalCard(shape = SpiceTheme.containerShape(), onClick = onClick) {
                                                ListItem(headlineContent = { Text(title) }, supportingContent = { Text(summary) }, colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent))
                                            }
                                        }
                                    }
                                }

                                // ── Group: Cabinet Utility / SubScreen / Controller ──
                                item {
                                    val group2 = listOf(
                                        Triple(strings.cabinetUtility, strings.keypadSummary, { maybeVibrate(15); actions.onOpenCabinetUtility() }),
                                        Triple(strings.subScreen, strings.subScreenSummary, { maybeVibrate(15); actions.onOpenSubScreen() }),
                                        Triple("${strings.gameController}${strings.classicSuffix}", strings.gameControllerSummary, { maybeVibrate(15); actions.onOpenController() }),
                                    )
                                    group2.forEach { (title, summary, onClick) ->
                                        Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                                            TonalCard(shape = SpiceTheme.containerShape(), onClick = onClick) {
                                                ListItem(headlineContent = { Text(title) }, supportingContent = { Text(summary) }, colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent))
                                            }
                                        }
                                    }
                                }

                                // ── Dev Mode: Patches / LCD / Resize / DIY ──
                                if (devMode) {
                                    item {
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                        val devItems = listOf(
                                            Triple(strings.patches, strings.patchesSummary, { maybeVibrate(15); actions.onOpenPatches() }),
                                            Triple(strings.lcdInfo, strings.lcdInfoSummary, { maybeVibrate(15); actions.onOpenLcd() }),
                                            Triple(strings.screenResize, strings.screenResizeSummary, { maybeVibrate(15); actions.onOpenResize() }),
                                            Triple(strings.diyController, strings.diyControllerSummary, { maybeVibrate(15); actions.onOpenDiy() }),
                                        )
                                        devItems.forEach { (title, summary, onClick) ->
                                            Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                                                TonalCard(shape = SpiceTheme.containerShape(), onClick = onClick) {
                                                    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(summary) }, colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent))
                                                }
                                            }
                                        }
                                    }
                                }

                                item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolsPagerMaterial(actions: ToolsScreenActions) {
    val strings = LocalAppStrings.current
    val fullscreen = LocalFullscreenMode.current
    val devMode = LocalDevMode.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            AdaptiveTopAppBar(
                title = { Text(strings.tools) },
                actions = { FullscreenAction() },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val topPad = if (fullscreen.value) 0.dp else innerPadding.calculateTopPadding()
        WideContentBox { LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(top = topPad),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)) }

            // ── Group: Buttons / Analogs / Lights ──
            item {
                val group1 = listOf(
                    Triple(strings.buttons, strings.buttonsSummary, { maybeVibrate(15); actions.onOpenButtons() }),
                    Triple(strings.analogs, strings.analogsSummary, { maybeVibrate(15); actions.onOpenAnalogs() }),
                    Triple(strings.lights, strings.lightsSummary, { maybeVibrate(15); actions.onOpenLights() }),
                )
                group1.forEach { (title, summary, onClick) ->
                    Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                        TonalCard(shape = SpiceTheme.containerShape(), onClick = onClick) {
                            ListItem(headlineContent = { Text(title) }, supportingContent = { Text(summary) }, colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent))
                        }
                    }
                }
            }

            // ── Group: Cabinet Utility / SubScreen / Controller ──
            item {
                val group2 = listOf(
                    Triple(strings.cabinetUtility, strings.keypadSummary, { maybeVibrate(15); actions.onOpenCabinetUtility() }),
                    Triple(strings.subScreen, strings.subScreenSummary, { maybeVibrate(15); actions.onOpenSubScreen() }),
                    Triple("${strings.gameController}${strings.classicSuffix}", strings.gameControllerSummary, { maybeVibrate(15); actions.onOpenController() }),
                )
                group2.forEach { (title, summary, onClick) ->
                    Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                        TonalCard(shape = SpiceTheme.containerShape(), onClick = onClick) {
                            ListItem(headlineContent = { Text(title) }, supportingContent = { Text(summary) }, colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent))
                        }
                    }
                }
            }

            // ── Dev Mode: Patches / LCD / Resize / DIY ──
            if (devMode) {
                item {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    val devItems = listOf(
                        Triple(strings.patches, strings.patchesSummary, { maybeVibrate(15); actions.onOpenPatches() }),
                        Triple(strings.lcdInfo, strings.lcdInfoSummary, { maybeVibrate(15); actions.onOpenLcd() }),
                        Triple(strings.screenResize, strings.screenResizeSummary, { maybeVibrate(15); actions.onOpenResize() }),
                        Triple(strings.diyController, strings.diyControllerSummary, { maybeVibrate(15); actions.onOpenDiy() }),
                    )
                    devItems.forEach { (title, summary, onClick) ->
                        Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                            TonalCard(shape = SpiceTheme.containerShape(), onClick = onClick) {
                                ListItem(headlineContent = { Text(title) }, supportingContent = { Text(summary) }, colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent))
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        } }
    }
}