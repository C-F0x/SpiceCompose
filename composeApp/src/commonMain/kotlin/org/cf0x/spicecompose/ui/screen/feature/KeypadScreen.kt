package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.data.CardConfig
import org.cf0x.spicecompose.data.CardRepository
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.cardInsert
import org.cf0x.spicecompose.network.spiceapi.wrappers.controlRestart
import org.cf0x.spicecompose.network.spiceapi.wrappers.controlShutdown
import org.cf0x.spicecompose.network.spiceapi.wrappers.controlReboot
import org.cf0x.spicecompose.network.spiceapi.wrappers.keypadsWrite
import org.cf0x.spicecompose.platform.NfcManager
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.platform.nfcAvailable
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeypadScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val repository = remember { CardRepository() }
    var cards by remember { mutableStateOf(repository.getCards()) }
    var chosenCardId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardConfig?>(null) }
    
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val windowSize = LocalWindowSize.current
    val fullscreen = org.cf0x.spicecompose.platform.LocalFullscreenMode.current
    val p = ThemePreferences
    
    var currentMode by remember { mutableIntStateOf(0) }
    val modeColor = if (currentMode == 0) Color(0xFF008080) else Color(0xFF800080)
    val modeLabel = if (currentMode == 0) "P1" else "P2"

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    // NFC listener — only active when Dynamic is selected and NFC is available
    LaunchedEffect(connection, chosenCardId) {
        if (connection == null || !nfcAvailable) return@LaunchedEffect
        NfcManager.tagIdFlow.collect { id ->
            if (chosenCardId == null) {
                connection.cardInsert(currentMode, id)
                maybeVibrate(100)
            }
        }
    }

    val onInsert: (String) -> Unit = { id ->
        scope.launch {
            connection?.cardInsert(currentMode, id)
            maybeVibrate(100)
        }
    }

    val onKeyClick: (String) -> Unit = { key ->
        maybeVibrate(50)
        scope.launch {
            connection?.keypadsWrite(currentMode, key)
        }
    }

    val keys = listOf("7", "8", "9", "4", "5", "6", "1", "2", "3", "0", "00", ".")

    val showDialog = showAddDialog || editingCard != null
    if (showDialog) {
        CardEditDialog(
            show = true,
            card = editingCard,
            onSave = { cfg ->
                repository.addCard(cfg)
                cards = repository.getCards()
                showAddDialog = false
                editingCard = null
            },
            onDelete = {
                editingCard?.let {
                    repository.deleteCard(it.id)
                    if (chosenCardId == it.id) chosenCardId = null
                    cards = repository.getCards()
                }
                showAddDialog = false
                editingCard = null
            },
            onCancel = {
                showAddDialog = false
                editingCard = null
            }
        )
    }

    val isLarge = windowSize != WindowSize.Compact

    @Composable
    fun KeypadGrid() {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(keys) { key ->
                if (LocalUiMode.current == UiMode.Miuix) {
                    KeyButtonMiuix(key) { onKeyClick(if (key == ".") "D" else if (key == "00") "A" else key) }
                } else {
                    KeyButtonMaterial(key) { onKeyClick(if (key == ".") "D" else if (key == "00") "A" else key) }
                }
            }
        }
    }

    @Composable
    fun ControlBar() {
        if (LocalUiMode.current == UiMode.Miuix) {
            top.yukonga.miuix.kmp.basic.TextButton(
                text = modeLabel,
                onClick = { currentMode = (currentMode + 1) % 2 },
                modifier = Modifier.fillMaxWidth(),
                colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
            )
        } else {
            androidx.compose.material3.Button(
                onClick = { currentMode = (currentMode + 1) % 2 },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = modeColor)
            ) {
                androidx.compose.material3.Text(modeLabel)
            }
        }
    }

    @Composable
    fun ProcessControl() {
        val uiMode = LocalUiMode.current
        if (uiMode == UiMode.Miuix) {
            top.yukonga.miuix.kmp.basic.Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    top.yukonga.miuix.kmp.basic.Text("Process", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        top.yukonga.miuix.kmp.basic.TextButton(
                            text = "Restart", onClick = { scope.launch { connection?.controlRestart() } },
                            modifier = Modifier.weight(1f), enabled = connection != null
                        )
                        top.yukonga.miuix.kmp.basic.TextButton(
                            text = "Shutdown", onClick = { scope.launch { connection?.controlShutdown() } },
                            modifier = Modifier.weight(1f), enabled = connection != null,
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                        )
                        top.yukonga.miuix.kmp.basic.TextButton(
                            text = "Reboot", onClick = { scope.launch { connection?.controlReboot() } },
                            modifier = Modifier.weight(1f), enabled = connection != null
                        )
                    }
                }
            }
        } else {
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    androidx.compose.material3.Text("Process", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        androidx.compose.material3.Button(onClick = { scope.launch { connection?.controlRestart() } }, modifier = Modifier.weight(1f), enabled = connection != null) { androidx.compose.material3.Text("Restart") }
                        androidx.compose.material3.Button(onClick = { scope.launch { connection?.controlShutdown() } }, modifier = Modifier.weight(1f), enabled = connection != null,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error)) { androidx.compose.material3.Text("Shutdown") }
                        androidx.compose.material3.OutlinedButton(onClick = { scope.launch { connection?.controlReboot() } }, modifier = Modifier.weight(1f), enabled = connection != null) { androidx.compose.material3.Text("Reboot") }
                    }
                }
            }
        }
    }

    @Composable
    fun CardManagement() {
        val uiMode = LocalUiMode.current
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiMode == UiMode.Miuix) {
                    top.yukonga.miuix.kmp.basic.Text("Card Management", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    top.yukonga.miuix.kmp.basic.IconButton(onClick = { showAddDialog = true }) {
                        top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Add, null)
                    }
                } else {
                    androidx.compose.material3.Text("Card Management", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.weight(1f))
                    androidx.compose.material3.IconButton(onClick = { showAddDialog = true }) {
                        androidx.compose.material3.Icon(Icons.Rounded.Add, null)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiMode == UiMode.Miuix) {
                    PillMiuix(
                        label = "Dynamic",
                        selected = chosenCardId == null,
                        enabled = nfcAvailable,
                        onClick = { if (nfcAvailable) chosenCardId = null },
                        onLongClick = {}
                    )
                    cards.forEach { card ->
                        PillMiuix(
                            label = card.name,
                            selected = chosenCardId == card.id,
                            enabled = true,
                            onClick = {
                                scope.launch {
                                    chosenCardId = card.id
                                    onInsert(card.cardId)
                                    chosenCardId = null // roll back to Dynamic
                                }
                            },
                            onLongClick = { editingCard = card }
                        )
                    }
                } else {
                    PillMaterial(
                        label = "Dynamic",
                        selected = chosenCardId == null,
                        enabled = nfcAvailable,
                        onClick = { if (nfcAvailable) chosenCardId = null },
                        onLongClick = {}
                    )
                    cards.forEach { card ->
                        PillMaterial(
                            label = card.name,
                            selected = chosenCardId == card.id,
                            enabled = true,
                            onClick = {
                                scope.launch {
                                    chosenCardId = card.id
                                    onInsert(card.cardId)
                                    chosenCardId = null // roll back to Dynamic
                                }
                            },
                            onLongClick = { editingCard = card }
                        )
                    }
                }
            }
        }
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        SmallTopAppBar(
                            title = strings.keypadScanner,
                            navigationIcon = { IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                            actions = { FullscreenAction() }
                        )
                    }
                }
            ) { innerPadding ->
                val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
                if (isLarge) {
                    Row(Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(1f)) { KeypadGrid(); Spacer(Modifier.height(16.dp)); ControlBar(); ProcessControl() }
                        Box(Modifier.weight(1f)) { CardManagement() }
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
                        KeypadGrid(); Spacer(Modifier.height(16.dp)); ControlBar(); ProcessControl()
                        Spacer(Modifier.height(24.dp)); CardManagement()
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
                            title = { androidx.compose.material3.Text(strings.keypadScanner) },
                            navigationIcon = { androidx.compose.material3.IconButton(onClick = onBack) { androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                            actions = { FullscreenAction() }
                        )
                    }
                }
            ) { innerPadding ->
                val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
                if (isLarge) {
                    Row(Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(1f)) { KeypadGrid(); Spacer(Modifier.height(16.dp)); ControlBar(); ProcessControl() }
                        Box(Modifier.weight(1f)) { CardManagement() }
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
                        KeypadGrid(); Spacer(Modifier.height(16.dp)); ControlBar(); ProcessControl()
                        Spacer(Modifier.height(24.dp)); CardManagement()
                    }
                }
            }
        }
    }
}

// ── Key buttons ──

@Composable
fun KeyButtonMiuix(label: String, onClick: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(modifier = Modifier.aspectRatio(1.5f), onClick = onClick) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            top.yukonga.miuix.kmp.basic.Text(label, fontSize = 28.sp)
        }
    }
}

@Composable
fun KeyButtonMaterial(label: String, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.aspectRatio(1.5f), onClick = onClick) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(label, fontSize = 28.sp)
        }
    }
}

// ── Card pills ──

@Composable
fun PillMiuix(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = when {
        !enabled -> MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        selected -> MiuixTheme.colorScheme.primary
        else -> MiuixTheme.colorScheme.surfaceVariant
    }
    val txtColor = when {
        !enabled -> MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        selected -> Color.White
        else -> MiuixTheme.colorScheme.onSurface
    }
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.height(36.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = bgColor),
        onClick = { if (enabled) onClick() },
        onLongPress = { if (enabled) onLongClick() }
    ) {
        Box(Modifier.fillMaxHeight().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            top.yukonga.miuix.kmp.basic.Text(label, fontSize = 14.sp, color = txtColor)
        }
    }
}

@Composable
fun PillMaterial(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val txtColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.height(36.dp).combinedClickable(
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick
        ),
        shape = MaterialTheme.shapes.medium,
        color = bgColor
    ) {
        Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(label, fontSize = 14.sp, color = txtColor)
        }
    }
}
