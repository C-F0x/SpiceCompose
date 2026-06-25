package org.cf0x.spicecompose.ui.screen.controllers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoAVS
import org.cf0x.spicecompose.platform.GameOptimizationEffect
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.screen.controllers.control.bbc.BbcController
import org.cf0x.spicecompose.ui.screen.controllers.control.ddr.DdrController
import org.cf0x.spicecompose.ui.screen.controllers.control.drs.DrsController
import org.cf0x.spicecompose.ui.screen.controllers.control.ftt.FttController
import org.cf0x.spicecompose.ui.screen.controllers.control.hpm.HpmController
import org.cf0x.spicecompose.ui.screen.controllers.control.iidx.IidxController
import org.cf0x.spicecompose.ui.screen.controllers.control.jb.JbController
import org.cf0x.spicecompose.ui.screen.controllers.control.lp.LpController
import org.cf0x.spicecompose.ui.screen.controllers.control.nost.NostController
import org.cf0x.spicecompose.ui.screen.controllers.control.popn.PopnController
import org.cf0x.spicecompose.ui.screen.controllers.control.rf3d.Rf3dController
import org.cf0x.spicecompose.ui.screen.controllers.control.sdvx.SdvxController
import org.cf0x.spicecompose.ui.screen.controllers.control.we.WeController
import org.cf0x.spicecompose.ui.screen.controllers.control.xif.XifController
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

// ── Game-model → controller mapping ──────────────────────────────────────

val gameModelToController: Map<String, String> = mapOf(
    // Jubeat
    "J44" to "jb",  "K44" to "jb",  "L44" to "jb",
    // IIDX
    "JDZ" to "iidx", "KDZ" to "iidx", "LDJ" to "iidx", "TDJ" to "iidx",
    // Pop'n Music
    "K39" to "popn", "L39" to "popn", "M39" to "popn",
    // Nostalgia
    "PAN" to "nost",
    // SDVX
    "KFC" to "sdvx", "UFC" to "sdvx",
    // DDR
    "JDX" to "ddr", "KDX" to "ddr", "MDX" to "ddr", "TDX" to "ddr",
    // Bishi Bashi Channel
    "R66" to "bbc",
    // Hello Pop'n
    "JMP" to "hpm",
    // Road Fighters 3D
    "JGT" to "rf3d",
    // Future TomTom
    "MMD" to "ftt",
    // LovePlus
    "KLP" to "lp",
    // DANCERUSH
    "REC" to "drs",
    // Polaris Chord
    "XIF" to "xif",
    // Winning Eleven
    "KCK" to "we",   "NCK" to "we",
)

/** All unique controller names for the debug picker. */
private val allControllerNames = gameModelToController.values.distinct().sorted()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerScreen(
    connectionManager: ConnectionManager,
    onBack: () -> Unit,
) {
    val fullscreen  = LocalFullscreenMode.current
    val uiMode      = LocalUiMode.current
    val p           = ThemePreferences

    // ── State ────────────────────────────────────────────────────────────
    var gameModel    by remember { mutableStateOf<String?>(null) }
    var gameInfo     by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var subViewIndex by remember { mutableIntStateOf(0) }
    var debugOverride by remember { mutableStateOf<String?>(null) }
    var debugExpanded by remember { mutableStateOf(false) }

    val effectiveController = debugOverride ?: gameModel?.let { gameModelToController[it] }

    // Enable high refresh rate + sustained performance while controller is active
    GameOptimizationEffect()

    // ── Poll game info every second (skipped when debug override is active) ─
    LaunchedEffect(connectionManager, debugOverride) {
        while (debugOverride == null) {
            val client = connectionManager.getClient()
            if (client != null) {
                try {
                    val info = client.infoAVS()
                    gameInfo = info
                    gameModel = info["model"]?.ifEmpty { null }
                } catch (_: Exception) {
                    gameModel = null; gameInfo = emptyMap()
                }
            } else {
                gameModel = null; gameInfo = emptyMap()
            }
            delay(1000)
        }
    }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    // ── Top bar: dual-mode MIUiX / Material ──────────────────────────────
    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    SmallTopAppBar(
                        title = "Game Controller",
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                            }
                        },
                        actions = {
                            CycleSubViewButton(effectiveController) { subViewIndex++ }
                            DebugOverrideButton(debugExpanded, { debugExpanded = it }, { debugOverride = it; subViewIndex = 0 })
                            FullscreenAction()
                        },
                    )
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            ControllerBody(effectiveController, gameModel, connectionManager, subViewIndex, pad)
        }
    } else {
        Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    TopAppBar(
                        title = { Text("Game Controller") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            CycleSubViewButton(effectiveController) { subViewIndex++ }
                            DebugOverrideButton(debugExpanded, { debugExpanded = it }, { debugOverride = it; subViewIndex = 0 })
                            FullscreenAction()
                        },
                    )
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            ControllerBody(effectiveController, gameModel, connectionManager, subViewIndex, pad)
        }
    }
}

// ── Cycle sub-view button ────────────────────────────────────────────────

@Composable
private fun CycleSubViewButton(
    activeController: String?,
    onClick: () -> Unit,
) {
    if (activeController != null) {
        if (LocalUiMode.current == UiMode.Miuix) {
            top.yukonga.miuix.kmp.basic.IconButton(onClick = onClick) {
                top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Autorenew, "Cycle view")
            }
        } else {
            IconButton(onClick = onClick) {
                Icon(Icons.Rounded.Autorenew, contentDescription = "Cycle view")
            }
        }
    }
}

// ── Debug override button ────────────────────────────────────────────────

@Composable
private fun DebugOverrideButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String?) -> Unit,
) {
    Box {
        if (LocalUiMode.current == UiMode.Miuix) {
            top.yukonga.miuix.kmp.basic.IconButton(onClick = { onExpandedChange(true) }) {
                top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.BugReport, "Debug select")
            }
            // MIUiX dropdown — use Material DropdownMenu as MIUiX doesn't have one
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                DropdownMenuItem(
                    text = { Text("(Auto-detect)") },
                    onClick = { onSelect(null); onExpandedChange(false) },
                )
                allControllerNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name.uppercase()) },
                        onClick = { onSelect(name); onExpandedChange(false) },
                    )
                }
            }
        } else {
            IconButton(onClick = { onExpandedChange(true) }) {
                Icon(Icons.Rounded.BugReport, contentDescription = "Debug select")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                DropdownMenuItem(
                    text = { Text("(Auto-detect)") },
                    onClick = { onSelect(null); onExpandedChange(false) },
                )
                allControllerNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name.uppercase()) },
                        onClick = { onSelect(name); onExpandedChange(false) },
                    )
                }
            }
        }
    }
}

// ── Body: route to controller or placeholder ─────────────────────────────

@Composable
private fun ControllerBody(
    controller: String?,
    gameModel: String?,
    connectionManager: ConnectionManager,
    subViewIndex: Int,
    padding: PaddingValues,
) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        when {
            controller == "bbc"  -> BbcController(connectionManager, subViewIndex)
            controller == "ddr"  -> DdrController(connectionManager, subViewIndex)
            controller == "drs"  -> DrsController(connectionManager, subViewIndex)
            controller == "ftt"  -> FttController(connectionManager, subViewIndex)
            controller == "hpm"  -> HpmController(connectionManager, subViewIndex)
            controller == "iidx" -> IidxController(connectionManager, subViewIndex)
            controller == "jb"   -> JbController(connectionManager, subViewIndex)
            controller == "lp"   -> LpController(connectionManager, subViewIndex)
            controller == "nost" -> NostController(connectionManager, subViewIndex)
            controller == "popn" -> PopnController(connectionManager, subViewIndex)
            controller == "rf3d" -> Rf3dController(connectionManager, subViewIndex)
            controller == "sdvx" -> SdvxController(connectionManager, subViewIndex)
            controller == "we"   -> WeController(connectionManager, subViewIndex)
            controller == "xif"  -> XifController(connectionManager, subViewIndex)
            controller != null -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Rounded.Gamepad, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text(controller.uppercase(), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Controller layout coming soon", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (subViewIndex > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text("Sub-view #$subViewIndex", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            gameModel != null -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text("Game: $gameModel", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("This game does not yet have a controller view :(", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> Text("Please connect to a server first.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
    }
}
