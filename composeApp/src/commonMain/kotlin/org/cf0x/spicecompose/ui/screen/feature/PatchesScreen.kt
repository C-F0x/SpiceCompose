package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.data.PatchConfig
import org.cf0x.spicecompose.data.PatchRepository
import org.cf0x.spicecompose.data.PatchStatus
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoAVS
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PatchesScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val repository = remember { PatchRepository() }
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val windowSize = LocalWindowSize.current
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    var customPatches by remember { mutableStateOf(repository.getCustomPatches()) }
    var presetPatches by remember { mutableStateOf<List<PatchConfig>>(emptyList()) }
    
    val patchStates = remember { mutableStateMapOf<String, PatchStatus>() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Preset", "Custom")

    LaunchedEffect(connection) {
        if (connection == null) return@LaunchedEffect
        try {
            val avs = connection.infoAVS()
            val dateCode = avs["ext"]?.toIntOrNull() ?: 0
            val allVisible = customPatches.filter { it.isInRange(dateCode) }
            allVisible.forEach { patch ->
                scope.launch {
                    patchStates[patch.name] = patch.getStatus(connection)
                }
            }
        } catch (_: Exception) { }
    }

    val onToggle: (PatchConfig) -> Unit = { patch ->
        scope.launch {
            val current = patchStates[patch.name] ?: PatchStatus.Disabled
            val next = if (current == PatchStatus.Enabled) PatchStatus.Disabled else PatchStatus.Enabled
            if (connection != null && patch.setStatus(connection, next)) {
                patchStates[patch.name] = patch.getStatus(connection)
            }
        }
    }

    val columns = if (windowSize == WindowSize.Compact) 1 else 2

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        SmallTopAppBar(
                            title = strings.patches,
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                                }
                            },
                            actions = {
                                FullscreenAction()
                            }
                        )
                    }
                }
            ) { innerPadding ->
                val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center) {
                        tabs.forEachIndexed { index, title ->
                            top.yukonga.miuix.kmp.basic.TextButton(
                                text = title,
                                onClick = { selectedTab = index },
                                colors = if (selectedTab == index) top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary() else top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColors()
                            )
                        }
                    }
                    
                    val list = if (selectedTab == 0) presetPatches else customPatches
                    if (list.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            top.yukonga.miuix.kmp.basic.Text("No patches found")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(list) { patch ->
                                PatchItemMiuix(patch, patchStates[patch.name] ?: PatchStatus.Unknown, onToggle)
                            }
                        }
                    }
                }
            }
        }
        UiMode.Material -> {
            androidx.compose.material3.Scaffold(
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        @OptIn(ExperimentalMaterial3Api::class)
                        androidx.compose.material3.TopAppBar(
                            title = { androidx.compose.material3.Text(strings.patches) },
                            navigationIcon = {
                                androidx.compose.material3.IconButton(onClick = onBack) {
                                    androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                                }
                            },
                            actions = {
                                FullscreenAction()
                            }
                        )
                    }
                }
            ) { innerPadding ->
                val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
                Column(Modifier.fillMaxSize().padding(padding)) {
                    SecondaryTabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { androidx.compose.material3.Text(title) })
                        }
                    }
                    
                    val list = if (selectedTab == 0) presetPatches else customPatches
                    if (list.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.Text("No patches found")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ) {
                            items(list) { patch ->
                                PatchItemMaterial(patch, patchStates[patch.name] ?: PatchStatus.Unknown, onToggle)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatchItemMiuix(patch: PatchConfig, status: PatchStatus, onToggle: (PatchConfig) -> Unit) {
    val statusColor = when (status) {
        PatchStatus.Enabled -> Color.Green
        PatchStatus.Disabled -> Color.Red
        PatchStatus.Unknown -> Color.Gray
    }
    val statusText = when (status) {
        PatchStatus.Enabled -> "(Enabled)"
        PatchStatus.Disabled -> "(Disabled)"
        PatchStatus.Unknown -> ""
    }

    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        onClick = { onToggle(patch) }
    ) {
        Column(Modifier.padding(16.dp)) {
            top.yukonga.miuix.kmp.basic.Text(
                text = "${patch.name} $statusText", 
                color = if (status != PatchStatus.Unknown) statusColor else MiuixTheme.colorScheme.onSurface,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            if (patch.description.isNotEmpty()) {
                top.yukonga.miuix.kmp.basic.Text(
                    patch.description, 
                    fontSize = 14.sp, 
                    color = MiuixTheme.colorScheme.onSurfaceVariantActions
                )
            }
        }
    }
}

@Composable
fun PatchItemMaterial(patch: PatchConfig, status: PatchStatus, onToggle: (PatchConfig) -> Unit) {
    val statusColor = when (status) {
        PatchStatus.Enabled -> Color.Green
        PatchStatus.Disabled -> Color.Red
        PatchStatus.Unknown -> Color.Gray
    }
    val statusText = when (status) {
        PatchStatus.Enabled -> "(Enabled)"
        PatchStatus.Disabled -> "(Disabled)"
        PatchStatus.Unknown -> ""
    }

    ListItem(
        modifier = Modifier.clickable { onToggle(patch) },
        headlineContent = { 
            androidx.compose.material3.Text(
                "${patch.name} $statusText", 
                color = if (status != PatchStatus.Unknown) statusColor else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            ) 
        },
        supportingContent = { if (patch.description.isNotEmpty()) androidx.compose.material3.Text(patch.description) }
    )
}
