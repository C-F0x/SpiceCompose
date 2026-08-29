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
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import org.cf0x.spicecompose.util.simpleFormat
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerScreen(
    connectionManager: ConnectionManager,
    onBack: () -> Unit,
) {
    val fullscreen = LocalFullscreenMode.current
    val uiMode     = LocalUiMode.current
    val p          = CustomPreferences
    val strings    = LocalAppStrings.current

    var gameModel       by remember { mutableStateOf<String?>(null) }
    var subViewIndex    by remember { mutableIntStateOf(0) }
    var debugOverride   by remember { mutableStateOf<ControllerLayout?>(null) }
    var forceUnsupported by remember { mutableStateOf(false) }
    var debugExpanded   by remember { mutableStateOf(false) }
    var subViewExpanded by remember { mutableStateOf(false) }

    val effectiveLayout = when {
        forceUnsupported -> null
        debugOverride != null -> debugOverride
        else -> gameModel?.let { layoutByModel[it] }
    }

    GameOptimizationEffect()

    LaunchedEffect(connectionManager, debugOverride) {
        while (debugOverride == null && !forceUnsupported) {
            val client = connectionManager.getClient()
            if (client != null) {
                try { gameModel = client.infoAVS()["model"]?.ifEmpty { null } }
                catch (_: Exception) { gameModel = null }
            } else { gameModel = null }
            delay(1000)
        }
    }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    if (uiMode == UiMode.Miuix) {
        MiuixScaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    SmallTopAppBar(
                        title = strings.gameController,
                        navigationIcon = {
                            MiuixIconButton(onClick = onBack) {
                                MiuixIcon(MiuixIcons.Back, null)
                            }
                        },
                        actions = {
                            if (effectiveLayout != null && effectiveLayout.subViews.isNotEmpty()) {
                                SubViewMenuButton(subViewExpanded, { subViewExpanded = it },
                                    effectiveLayout.subViews, subViewIndex,
                                    { subViewIndex = it; subViewExpanded = false },
                                )
                            }
                            if (p.devMode) {
                                DebugOverrideButton(debugExpanded, { debugExpanded = it },
                                    { debugOverride = it; forceUnsupported = false; subViewIndex = 0 },
                                    { forceUnsupported = true; debugOverride = null; debugExpanded = false; subViewIndex = 0 },
                                )
                            }
                            FullscreenAction()
                        },
                    )
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            ControllerBody(effectiveLayout, gameModel, connectionManager, subViewIndex, pad, forceUnsupported)
        }
    } else {
        Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    AdaptiveTopAppBar(
                        title = { Text(strings.gameController) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backDesc)
                            }
                        },
                        actions = {
                            if (effectiveLayout != null && effectiveLayout.subViews.isNotEmpty()) {
                                SubViewMenuButton(subViewExpanded, { subViewExpanded = it },
                                    effectiveLayout.subViews, subViewIndex,
                                    { subViewIndex = it; subViewExpanded = false },
                                )
                            }
                            if (p.devMode) {
                                DebugOverrideButton(debugExpanded, { debugExpanded = it },
                                    { debugOverride = it; forceUnsupported = false; subViewIndex = 0 },
                                    { forceUnsupported = true; debugOverride = null; debugExpanded = false; subViewIndex = 0 },
                                )
                            }
                            FullscreenAction()
                        },
                    )
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            ControllerBody(effectiveLayout, gameModel, connectionManager, subViewIndex, pad, forceUnsupported)
        }
    }
}

// ── SubView menu button (top-bar action) ──────────────────────────────────

@Composable
private fun SubViewMenuButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Box {
        if (LocalUiMode.current == UiMode.Miuix) {
            MiuixIconButton(onClick = { onExpandedChange(true) }, modifier = Modifier.size(35.dp)) {
                MiuixIcon(Icons.Rounded.ViewDay, null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                labels.forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = {
                            MiuixText(
                                label,
                                fontWeight = if (index == selected) androidx.compose.ui.text.font.FontWeight.Bold
                                             else androidx.compose.ui.text.font.FontWeight.Normal,
                                color = if (index == selected) MiuixTheme.colorScheme.primary
                                        else MiuixTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = { onSelect(index) },
                    )
                }
            }
        } else {
            IconButton(onClick = { onExpandedChange(true) }) {
                Icon(Icons.Rounded.ViewDay, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                labels.forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                label,
                                fontWeight = if (index == selected) androidx.compose.ui.text.font.FontWeight.Bold
                                             else androidx.compose.ui.text.font.FontWeight.Normal,
                                color = if (index == selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = { onSelect(index) },
                    )
                }
            }
        }
    }
}

// ── Debug override button (top-bar action, DevMode only) ──────────────────

@Composable
private fun DebugOverrideButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectLayout: (ControllerLayout?) -> Unit,
    onSelectUnsupported: () -> Unit,
) {
    Box {
        val strings = LocalAppStrings.current
        if (LocalUiMode.current == UiMode.Miuix) {
            MiuixIconButton(onClick = { onExpandedChange(true) }, modifier = Modifier.size(35.dp)) {
                MiuixIcon(Icons.Rounded.BugReport, strings.debugSelectDesc)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                DropdownMenuItem(
                    text = { MiuixText(strings.autoDetect) },
                    onClick = { onSelectLayout(null); onExpandedChange(false) },
                )
                DropdownMenuItem(
                    text = { MiuixText(strings.doesntSupport, color = MiuixTheme.colorScheme.error) },
                    onClick = { onSelectUnsupported() },
                )
                allLayouts.forEach { layout ->
                    DropdownMenuItem(
                        text = { MiuixText(layout.name) },
                        onClick = { onSelectLayout(layout); onExpandedChange(false) },
                    )
                }
            }
        } else {
            IconButton(onClick = { onExpandedChange(true) }) {
                Icon(Icons.Rounded.BugReport, contentDescription = strings.debugSelectDesc)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                DropdownMenuItem(
                    text = { Text(strings.autoDetect) },
                    onClick = { onSelectLayout(null); onExpandedChange(false) },
                )
                DropdownMenuItem(
                    text = { Text(strings.doesntSupport, color = MaterialTheme.colorScheme.error) },
                    onClick = { onSelectUnsupported() },
                )
                allLayouts.forEach { layout ->
                    DropdownMenuItem(
                        text = { Text(layout.name) },
                        onClick = { onSelectLayout(layout); onExpandedChange(false) },
                    )
                }
            }
        }
    }
}

// ── Body ──────────────────────────────────────────────────────────────────

@Composable
private fun ControllerBody(
    layout: ControllerLayout?,
    gameModel: String?,
    connectionManager: ConnectionManager,
    subViewIndex: Int,
    padding: PaddingValues,
    forceUnsupported: Boolean,
) {
    val strings = LocalAppStrings.current
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        when {
            layout != null -> layout.content(connectionManager, subViewIndex)
            forceUnsupported -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(Icons.Rounded.Gamepad, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Text(strings.doesntSupport, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text(strings.noControllerView, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            gameModel != null -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(strings.gameLabel.simpleFormat(gameModel), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(strings.noControllerView, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> Text(strings.connectFirst, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
    }
}
