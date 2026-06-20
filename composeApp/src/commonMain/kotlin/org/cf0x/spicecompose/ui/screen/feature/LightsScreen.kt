package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.LightState
import org.cf0x.spicecompose.network.spiceapi.wrappers.lightsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.lightsWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.lightsWriteReset
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
fun LightsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val windowSize = LocalWindowSize.current
    val fullscreen = org.cf0x.spicecompose.platform.LocalFullscreenMode.current
    val p = ThemePreferences
    
    var lightStates by remember { mutableStateOf<List<LightState>>(emptyList()) }
    val draggingNames = remember { mutableStateListOf<String>() }

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    // Polling logic
    LaunchedEffect(connection) {
        if (connection == null) {
            lightStates = emptyList()
            return@LaunchedEffect
        }
        while (isActive) {
            try {
                val newState = connection.lightsRead()
                lightStates = newState.map { fresh ->
                    if (draggingNames.contains(fresh.name)) {
                        lightStates.find { it.name == fresh.name } ?: fresh
                    } else {
                        fresh
                    }
                }
            } catch (_: Exception) { }
            delay(200)
        }
    }

    DisposableEffect(connection) {
        onDispose {
            scope.launch {
                connection?.lightsWriteReset(emptyList())
            }
        }
    }

    val onValueChange: (LightState, Float) -> Unit = { light, value ->
        val updated = light.copy(state = value.toDouble(), active = true)
        lightStates = lightStates.map { if (it.name == light.name) updated else it }
    }

    val onValueCommit: (LightState) -> Unit = { light ->
        scope.launch {
            connection?.lightsWrite(listOf(light))
        }
    }


    val columns = when (windowSize) {
        WindowSize.Compact -> 1
        else -> 2
    }

    val uiMode = LocalUiMode.current

    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    SmallTopAppBar(
                        title = strings.lights,
                        navigationIcon = { IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                        actions = {
                            FullscreenAction()
                        }
                    )
                }
            }
        ) { innerPadding ->
            val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            if (lightStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    top.yukonga.miuix.kmp.basic.Text("No lights available :(")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(lightStates) { light ->
                        LightItemMiuix(
                            light = light,
                            onValueChange = { onValueChange(light, it) },
                            onValueCommit = { onValueCommit(light) },
                            onDragStart = { draggingNames.add(light.name) },
                            onDragEnd = { draggingNames.remove(light.name) }
                        )
                    }
                }
            }
        }
    } else {
        androidx.compose.material3.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    androidx.compose.material3.TopAppBar(
                        title = { androidx.compose.material3.Text(strings.lights) },
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
            if (lightStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text("No lights available :(")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    items(lightStates) { light ->
                        LightItemMaterial(
                            light = light,
                            onValueChange = { onValueChange(light, it) },
                            onValueCommit = { onValueCommit(light) },
                            onDragStart = { draggingNames.add(light.name) },
                            onDragEnd = { draggingNames.remove(light.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LightItemMiuix(light: LightState, onValueChange: (Float) -> Unit, onValueCommit: () -> Unit, onDragStart: () -> Unit, onDragEnd: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            val titleColor = if (light.active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
            top.yukonga.miuix.kmp.basic.Text(light.name, color = titleColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            top.yukonga.miuix.kmp.basic.Slider(
                value = light.state.toFloat(),
                onValueChange = {
                    onDragStart()
                    onValueChange(it)
                },
                onValueChangeFinished = {
                    onDragEnd()
                    onValueCommit()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LightItemMaterial(light: LightState, onValueChange: (Float) -> Unit, onValueCommit: () -> Unit, onDragStart: () -> Unit, onDragEnd: () -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = {
            val titleColor = if (light.active) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            androidx.compose.material3.Text(light.name, color = titleColor)
        },
        supportingContent = {
            androidx.compose.material3.Slider(
                value = light.state.toFloat(),
                onValueChange = {
                    onDragStart()
                    onValueChange(it)
                },
                onValueChangeFinished = {
                    onDragEnd()
                    onValueCommit()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
