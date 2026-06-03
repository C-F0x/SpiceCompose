package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.AnalogState
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsWriteReset
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AnalogsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getConnection()
    val scope = rememberCoroutineScope()
    
    var analogStates by remember { mutableStateOf<List<AnalogState>>(emptyList()) }
    var locked by remember { mutableStateOf(false) }
    val draggingNames = remember { mutableStateListOf<String>() }

    // Polling logic
    LaunchedEffect(connection) {
        if (connection == null) {
            analogStates = emptyList()
            return@LaunchedEffect
        }
        
        while (isActive) {
            try {
                val newState = connection.analogsRead()
                // Update non-dragging ones
                analogStates = newState.map { fresh ->
                    if (draggingNames.contains(fresh.name)) {
                        analogStates.find { it.name == fresh.name } ?: fresh
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
                if (!locked) connection?.analogsWriteReset(emptyList())
            }
        }
    }

    val onValueChange: (AnalogState, Float) -> Unit = { analog, value ->
        val updated = analog.copy(state = value.toDouble(), active = true)
        analogStates = analogStates.map { if (it.name == analog.name) updated else it }
        // Local state update only
    }

    val onValueCommit: (AnalogState) -> Unit = { analog ->
        scope.launch {
            connection?.analogsWrite(listOf(analog))
        }
    }

    val onLockToggle: () -> Unit = {
        scope.launch {
            if (locked) {
                locked = false
                connection?.analogsWriteReset(emptyList())
            } else {
                locked = true
                connection?.analogsWrite(analogStates)
            }
        }
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    top.yukonga.miuix.kmp.basic.SmallTopAppBar(
                        title = strings.analogs,
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                            }
                        },
                        actions = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onLockToggle) {
                                top.yukonga.miuix.kmp.basic.Icon(if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                if (analogStates.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        top.yukonga.miuix.kmp.basic.Text("No analogs available :(")
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(innerPadding)) {
                        items(analogStates) { analog ->
                            AnalogItemMiuix(
                                analog = analog,
                                onValueChange = { onValueChange(analog, it) },
                                onValueCommit = { onValueCommit(analog) },
                                onDragStart = { draggingNames.add(analog.name) },
                                onDragEnd = { draggingNames.remove(analog.name) }
                            )
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
                        title = { androidx.compose.material3.Text(strings.analogs) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
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
                if (analogStates.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text("No analogs available :(")
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(innerPadding)) {
                        items(analogStates) { analog ->
                            AnalogItemMaterial(
                                analog = analog,
                                onValueChange = { onValueChange(analog, it) },
                                onValueCommit = { onValueCommit(analog) },
                                onDragStart = { draggingNames.add(analog.name) },
                                onDragEnd = { draggingNames.remove(analog.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalogItemMiuix(analog: AnalogState, onValueChange: (Float) -> Unit, onValueCommit: () -> Unit, onDragStart: () -> Unit, onDragEnd: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            val titleColor = if (analog.active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
            top.yukonga.miuix.kmp.basic.Text(analog.name, color = titleColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            top.yukonga.miuix.kmp.basic.Slider(
                value = analog.state.toFloat(),
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
fun AnalogItemMaterial(analog: AnalogState, onValueChange: (Float) -> Unit, onValueCommit: () -> Unit, onDragStart: () -> Unit, onDragEnd: () -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = {
            val titleColor = if (analog.active) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            androidx.compose.material3.Text(analog.name, color = titleColor)
        },
        supportingContent = {
            androidx.compose.material3.Slider(
                value = analog.state.toFloat(),
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
