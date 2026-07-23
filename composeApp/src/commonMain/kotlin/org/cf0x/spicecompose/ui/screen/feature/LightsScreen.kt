package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.cf0x.spicecompose.network.spiceapi.wrappers.GameLedDevice
import org.cf0x.spicecompose.network.spiceapi.wrappers.LightState
import org.cf0x.spicecompose.network.spiceapi.wrappers.lightsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.lightsWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.lightsWriteReset
import org.cf0x.spicecompose.network.spiceapi.wrappers.lightsReadGameSpecific
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoAVS
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.theme.CustomPreferences
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

@Composable
fun LightsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val windowSize = LocalWindowSize.current
    val fullscreen = org.cf0x.spicecompose.platform.LocalFullscreenMode.current
    val p = CustomPreferences
    
    var lightStates by remember { mutableStateOf<List<LightState>>(emptyList()) }
    val draggingNames = remember { mutableStateListOf<String>() }

    // ── Game-specific LED state ──────────────────────────────────────────
    var gameModel by remember { mutableStateOf<String?>(null) }
    var gameLedDevices by remember { mutableStateOf<List<GameLedDevice>>(emptyList()) }

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    // Polling logic
    LaunchedEffect(connection) {
        if (connection == null) {
            lightStates = emptyList()
            return@LaunchedEffect
        }
        while (isActive && connectionManager.getClient() != null) {
            try {
                val newState = connection.lightsRead()
                val dragSet = draggingNames.toSet()
                lightStates = if (dragSet.isEmpty()) {
                    newState
                } else {
                    newState.map { fresh ->
                        if (fresh.name in dragSet) lightStates.find { it.name == fresh.name } ?: fresh
                        else fresh
                    }
                }
            } catch (_: Exception) {
                if (connectionManager.getClient() == null) break
            }
            delay(200)
        }
    }

    DisposableEffect(connection) {
        onDispose {
            scope.launch(NonCancellable) {
                connection?.lightsWriteReset(emptyList())
            }
        }
    }

    // ── Game model detection + game-specific LED polling (every 2s) ────
    LaunchedEffect(Unit) {
        while (isActive) {
            val client = connectionManager.getClient()
            if (client != null) {
                try {
                    val avs = client.infoAVS()
                    val model = avs["model"]?.ifEmpty { null }
                    gameModel = model
                    if (model != null) {
                        val ledData = client.lightsReadGameSpecific(model)
                        if (ledData != null) gameLedDevices = ledData
                    }
                } catch (_: Exception) { }
            }
            delay(2000)
        }
    }

    // ── Slider write controller ─────────────────────────────────────────
    val sliderWrite = remember(connection) {
        SliderWriteController(
            nameSelector = { l: LightState -> l.name },
            writeBlock = { l -> connection?.lightsWrite(listOf(l)) }
        )
    }

    val onValueChange: (LightState, Float) -> Unit = { light, value ->
        val updated = light.copy(state = value.toDouble(), active = true)
        lightStates = lightStates.map { if (it.name == light.name) updated else it }
        sliderWrite.write(updated, scope)
    }

    val onValueCommit: (LightState) -> Unit = { light ->
        sliderWrite.commit(light, scope)
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
                            title = strings.lights,
                            navigationIcon = { IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                            actions = {
                                FullscreenAction()
                            },
                            color = barColor,
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            }
        ) { innerPadding ->
            val topPadding = innerPadding.calculateTopPadding()
            if (lightStates.isEmpty() && gameLedDevices.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = topPadding), contentAlignment = Alignment.Center) {
                    top.yukonga.miuix.kmp.basic.Text(strings.noLightsAvailable)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding().nestedScroll(scrollBehavior.nestedScrollConnection)
                        .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
                    contentPadding = PaddingValues(top = topPadding)
                ) {
                    item { Spacer(Modifier.height(12.dp)) }
                    items(lightStates) { light ->
                        LightItemMiuix(
                            light = light,
                            onValueChange = { onValueChange(light, it) },
                            onValueCommit = { onValueCommit(light) },
                            onDragStart = { draggingNames.add(light.name) },
                            onDragEnd = { draggingNames.remove(light.name) }
                        )
                    }
                    if (gameLedDevices.isNotEmpty()) {
                        @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            GameLedStrip(gameLedDevices)
                        }
                    }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                }
            }
        }
    } else {
        @OptIn(ExperimentalMaterial3Api::class)
        val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior()
        androidx.compose.material3.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    AdaptiveTopAppBar(
                        title = { androidx.compose.material3.Text(strings.lights) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                            }
                        },
                        actions = {
                            FullscreenAction()
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        ) { innerPadding ->
            val topPadding = innerPadding.calculateTopPadding()
            if (lightStates.isEmpty() && gameLedDevices.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = topPadding), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text(strings.noLightsAvailable)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(top = topPadding)
                ) {
                    item { Spacer(Modifier.height(12.dp)) }
                    items(lightStates) { light ->
                        LightItemMaterial(
                            light = light,
                            onValueChange = { onValueChange(light, it) },
                            onValueCommit = { onValueCommit(light) },
                            onDragStart = { draggingNames.add(light.name) },
                            onDragEnd = { draggingNames.remove(light.name) }
                        )
                    }
                    if (gameLedDevices.isNotEmpty()) {
                        @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            GameLedGridItem(gameLedDevices)
                        }
                    }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
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

// ── Game-specific LED visualization ──────────────────────────────────────

/**
 * 展示游戏专用 RGB LED 灯带/设备。
 * 每个设备一行，显示一排彩色 LED 方块。
 */
@Composable
fun GameLedStrip(devices: List<GameLedDevice>) {
    if (devices.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        top.yukonga.miuix.kmp.basic.Text(
            "Game LEDs",
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        devices.forEach { device ->
            Column(Modifier.padding(bottom = 8.dp)) {
                top.yukonga.miuix.kmp.basic.Text(
                    device.name,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.ScrollState(0)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    device.leds.forEach { rgb ->
                        if (rgb.size >= 3) {
                            val color = Color(rgb[0], rgb[1], rgb[2])
                            Box(
                                modifier = Modifier
                                    .size(if (device.leds.size > 30) 8.dp else 16.dp)
                                    .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 版本的 Game LED 可视化，放在 LazyVerticalGrid 的 item 中使用。
 */
@Composable
fun GameLedGridItem(devices: List<GameLedDevice>) {
    if (devices.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        androidx.compose.material3.Text(
            "Game LEDs",
            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        devices.forEach { device ->
            Column(Modifier.padding(bottom = 8.dp)) {
                androidx.compose.material3.Text(
                    device.name,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.ScrollState(0)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    device.leds.forEach { rgb ->
                        if (rgb.size >= 3) {
                            val color = Color(rgb[0], rgb[1], rgb[2])
                            Box(
                                modifier = Modifier
                                    .size(if (device.leds.size > 30) 8.dp else 16.dp)
                                    .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
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
