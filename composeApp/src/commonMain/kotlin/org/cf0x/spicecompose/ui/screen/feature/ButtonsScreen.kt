package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import org.cf0x.spicecompose.network.spiceapi.wrappers.ButtonState
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsWriteReset
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.platform.VibratorManager
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ButtonsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val fullscreen = LocalFullscreenMode.current
    val windowSize = LocalWindowSize.current
    
    // Auto exit fullscreen on dispose
    DisposableEffect(Unit) {
        onDispose {
            fullscreen.value = false
        }
    }

    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getConnection()
    val scope = rememberCoroutineScope()
    
    var buttonStates by remember { mutableStateOf<List<ButtonState>>(emptyList()) }
    var locked by remember { mutableStateOf(false) }

    // Polling logic
    LaunchedEffect(connection) {
        if (connection == null) {
            buttonStates = emptyList()
            return@LaunchedEffect
        }
        
        while (isActive) {
            try {
                val newState = connection.buttonsRead()
                buttonStates = newState
            } catch (_: Exception) { }
            delay(200)
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(connection) {
        onDispose {
            scope.launch {
                if (!locked) connection?.buttonsWriteReset(emptyList())
            }
        }
    }

    val onToggle: (ButtonState) -> Unit = { button ->
        VibratorManager.vibrate(50)
        scope.launch {
            connection?.buttonsWrite(listOf(button.copy(state = 1.0, active = true)))
            delay(100)
            connection?.buttonsWrite(listOf(button.copy(state = 0.0, active = true)))
        }
    }

    val onLockToggle: () -> Unit = {
        scope.launch {
            if (locked) {
                locked = false
                connection?.buttonsWriteReset(emptyList())
            } else {
                locked = true
                connection?.buttonsWrite(buttonStates)
            }
        }
    }

    val columns = when (windowSize) {
        WindowSize.Compact -> 1
        WindowSize.Medium -> 2
        WindowSize.Expanded -> 3
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = strings.buttons,
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = onLockToggle) {
                                top.yukonga.miuix.kmp.basic.Icon(if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                if (buttonStates.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        top.yukonga.miuix.kmp.basic.Text("No buttons available :(")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(buttonStates) { button ->
                            ButtonMiuix(button, onToggle)
                        }
                    }
                }
            }
        }
        UiMode.Material -> {
            androidx.compose.material3.Scaffold(
                topBar = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    androidx.compose.material3.TopAppBar(
                        title = { androidx.compose.material3.Text(strings.buttons) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        },
                        actions = {
                            androidx.compose.material3.IconButton(onClick = onLockToggle) {
                                androidx.compose.material3.Icon(if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = onLockToggle,
                        containerColor = if (locked) androidx.compose.material3.MaterialTheme.colorScheme.errorContainer else androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    ) {
                        androidx.compose.material3.Icon(if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null)
                    }
                }
            ) { innerPadding ->
                if (buttonStates.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text("No buttons available :(")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {
                        items(buttonStates) { button ->
                            ButtonMaterial(button, onToggle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonMiuix(button: ButtonState, onToggle: (ButtonState) -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        onClick = { onToggle(button) }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val titleColor = if (button.active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
            top.yukonga.miuix.kmp.basic.Text(button.name, color = titleColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            val statusText = if (button.state >= 0.5) "Pressed" else "Not Pressed"
            val statusColor = if (button.state >= 0.5) Color.Green else Color.Red
            top.yukonga.miuix.kmp.basic.Text(statusText, color = statusColor, fontSize = 14.sp)
        }
    }
}

@Composable
fun ButtonMaterial(button: ButtonState, onToggle: (ButtonState) -> Unit) {
    androidx.compose.material3.ListItem(
        modifier = Modifier.clickable { onToggle(button) },
        headlineContent = { 
            val titleColor = if (button.active) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            androidx.compose.material3.Text(button.name, color = titleColor) 
        },
        supportingContent = {
            val statusText = if (button.state >= 0.5) "Pressed" else "Not Pressed"
            val statusColor = if (button.state >= 0.5) Color.Green else Color.Red
            androidx.compose.material3.Text(statusText, color = statusColor)
        }
    )
}
