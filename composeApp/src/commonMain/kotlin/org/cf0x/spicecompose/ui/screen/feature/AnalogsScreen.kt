package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.AnalogState
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.analogsWriteReset
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize
import org.cf0x.spicecompose.ui.navigation.horizontalCutoutPadding
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalogsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val windowSize = LocalWindowSize.current
    val fullscreen = org.cf0x.spicecompose.platform.LocalFullscreenMode.current
    val p = CustomPreferences

    var analogStates by remember { mutableStateOf<List<AnalogState>>(emptyList()) }
    val draggingNames = remember { mutableStateListOf<String>() }

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    LaunchedEffect(connection) {
        if (connection == null) {
            analogStates = emptyList()
            return@LaunchedEffect
        }
        while (isActive && connectionManager.getClient() != null) {
            try {
                val newState = connection.analogsRead()
                val dragSet = draggingNames.toSet()
                analogStates = if (dragSet.isEmpty()) {
                    newState
                } else {
                    newState.map { fresh ->
                        if (fresh.name in dragSet) analogStates.find { it.name == fresh.name } ?: fresh
                        else fresh
                    }
                }
            } catch (_: Exception) {
                if (connectionManager.getClient() == null) break
            }
            delay(maxOf(128L, connectionManager.latencyMs.value))
        }
    }

    DisposableEffect(connection) {
        onDispose {
            scope.launch(NonCancellable) {
                connection?.analogsWriteReset(emptyList())
            }
        }
    }

    // ── Slider write controller ─────────────────────────────────────────
    val sliderWrite = remember(connection) {
        SliderWriteController(
            nameSelector = { a: AnalogState -> a.name },
            writeBlock = { a -> connection?.analogsWrite(listOf(a)) }
        )
    }

    val onValueChange: (AnalogState, Float) -> Unit = { analog, value ->
        val updated = analog.copy(state = value.toDouble(), active = true)
        analogStates = analogStates.map { if (it.name == analog.name) updated else it }
        sliderWrite.write(updated, scope)
    }

    val onValueCommit: (AnalogState) -> Unit = { analog ->
        sliderWrite.commit(analog, scope)
    }

    val columns = when (windowSize) {
        WindowSize.Compact -> 1
        else -> 2
    }

    val uiMode = LocalUiMode.current

    if (uiMode == UiMode.Miuix) {
        val scrollBehavior = MiuixScrollBehavior()
        val enableBlur = LocalEnableBlur.current
        val backdrop = rememberBlurBackdrop(enableBlur && LocalUiMode.current == UiMode.Miuix)
        val blurActive = backdrop != null
        val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    BlurredBar(backdrop, blurActive) {
                        SmallTopAppBar(
                            title = strings.analogs,
                            navigationIcon = { IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                            actions = { FullscreenAction() },
                            color = barColor,
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            }
        ) { innerPadding ->
            val topPadding = innerPadding.calculateTopPadding()
            if (analogStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = topPadding), contentAlignment = Alignment.Center) {
                    top.yukonga.miuix.kmp.basic.Text(strings.noAnalogsAvailable)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding().nestedScroll(scrollBehavior.nestedScrollConnection)
                        .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
                    contentPadding = PaddingValues(top = topPadding + 12.dp)
                ) {
                    items(analogStates) { analog ->
                        AnalogItemMiuix(
                            analog = analog,
                            onValueChange = { onValueChange(analog, it) },
                            onValueCommit = { onValueCommit(analog) },
                            onDragStart = { draggingNames.add(analog.name) },
                            onDragEnd = { draggingNames.remove(analog.name) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                }
            }
        }
    } else {
        val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior()
        androidx.compose.material3.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    AdaptiveTopAppBar(
                        title = { androidx.compose.material3.Text(strings.analogs) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                            }
                        },
                        actions = { FullscreenAction() },
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        ) { innerPadding ->
            val topPadding = innerPadding.calculateTopPadding()
            if (analogStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = topPadding), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text(strings.noAnalogsAvailable)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(top = topPadding + 12.dp)
                ) {
                    items(analogStates) { analog ->
                        AnalogItemMaterial(
                            analog = analog,
                            onValueChange = { onValueChange(analog, it) },
                            onValueCommit = { onValueCommit(analog) },
                            onDragStart = { draggingNames.add(analog.name) },
                            onDragEnd = { draggingNames.remove(analog.name) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
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
