package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.data.CardConfig
import org.cf0x.spicecompose.data.CardRepository
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.cardInsert
import org.cf0x.spicecompose.network.spiceapi.wrappers.keypadsWrite
import org.cf0x.spicecompose.platform.NfcManager
import org.cf0x.spicecompose.platform.VibratorManager
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.TonalCard
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
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
    var chosenCardId by remember { mutableStateOf<String?>(null) } // null means Dynamic
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardConfig?>(null) }
    
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getConnection()
    val scope = rememberCoroutineScope()
    
    var currentMode by remember { mutableIntStateOf(0) } // 0: P1, 1: P2
    val modeColor = if (currentMode == 0) Color(0xFF008080) else Color(0xFF800080)
    val modeLabel = if (currentMode == 0) "P1" else "P2"

    // Listen for NFC tags
    LaunchedEffect(connection, chosenCardId) {
        if (connection == null) return@LaunchedEffect
        NfcManager.tagIdFlow.collect { id ->
            if (chosenCardId == null) {
                // Dynamic mode: insert whatever we scan
                connection.cardInsert(currentMode, id)
                VibratorManager.vibrate(100)
            } else {
                // Fixed mode: NFC scan doesn't auto-insert (user must tap pill)
                // OR we can make it so it inserts the CHOSEN card when any tag is detected?
                // spicecompanion says: "Dynamic... dynamic reading. Chosen... insert CHOSEN card."
                // I'll stick to: if chosen, ignore NFC, only insert on pill click.
            }
        }
    }

    val onInsert: (String) -> Unit = { id ->
        scope.launch {
            connection?.cardInsert(currentMode, id)
            VibratorManager.vibrate(100)
        }
    }

    val onKeyClick: (String) -> Unit = { key ->
        VibratorManager.vibrate(50)
        scope.launch {
            connection?.keypadsWrite(currentMode, key)
        }
    }

    val keys = listOf(
        "7", "8", "9",
        "4", "5", "6",
        "1", "2", "3",
        "0", "00", "."
    )

    if (showAddDialog || editingCard != null) {
        CardEditDialog(
            show = true,
            card = editingCard,
            onSave = {
                if (editingCard != null) repository.updateCard(it) else repository.addCard(it)
                cards = repository.getCards()
                showAddDialog = false
                editingCard = null
            },
            onDiscard = {
                editingCard?.let { 
                    repository.deleteCard(it.id)
                    if (chosenCardId == it.id) chosenCardId = null
                    cards = repository.getCards()
                }
                showAddDialog = false // Also for new card case
                editingCard = null
            },
            onDismiss = {
                showAddDialog = false
                editingCard = null
            }
        )
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = strings.keypadScanner,
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(keys) { key ->
                            KeyButtonMiuix(key) { onKeyClick(if (key == ".") "D" else if (key == "00") "A" else key) }
                        }
                    }
                    
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        top.yukonga.miuix.kmp.basic.TextButton(
                            text = modeLabel,
                            onClick = { currentMode = (currentMode + 1) % 2 },
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                        )
                    }

                    // Card Management Container
                    top.yukonga.miuix.kmp.basic.Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                top.yukonga.miuix.kmp.basic.Text("Card Management", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                top.yukonga.miuix.kmp.basic.IconButton(onClick = { showAddDialog = true }) {
                                    top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Add, null)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            // Pills row
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PillMiuix(
                                    label = "Dynamic", 
                                    selected = chosenCardId == null, 
                                    onClick = { chosenCardId = null },
                                    onLongClick = {}
                                )
                                cards.forEach { card ->
                                    PillMiuix(
                                        label = card.name, 
                                        selected = chosenCardId == card.id, 
                                        onClick = { 
                                            chosenCardId = card.id
                                            onInsert(card.cardId)
                                        },
                                        onLongClick = { editingCard = card }
                                    )
                                }
                            }
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
                        title = { androidx.compose.material3.Text(strings.keypadScanner) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(keys) { key ->
                            KeyButtonMaterial(key) { onKeyClick(if (key == ".") "D" else if (key == "00") "A" else key) }
                        }
                    }
                    
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        androidx.compose.material3.Button(
                            onClick = { currentMode = (currentMode + 1) % 2 },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = modeColor)
                        ) {
                            androidx.compose.material3.Text(modeLabel)
                        }
                    }

                    // Card Management Container
                    TonalCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Text("Card Management", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.weight(1f))
                                androidx.compose.material3.IconButton(onClick = { showAddDialog = true }) {
                                    androidx.compose.material3.Icon(Icons.Rounded.Add, null)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = chosenCardId == null,
                                    onClick = { chosenCardId = null },
                                    label = { androidx.compose.material3.Text("Dynamic") }
                                )
                                cards.forEach { card ->
                                    Box(Modifier.combinedClickable(
                                        onClick = { 
                                            chosenCardId = card.id
                                            onInsert(card.cardId)
                                        },
                                        onLongClick = { editingCard = card }
                                    )) {
                                        FilterChip(
                                            selected = chosenCardId == card.id,
                                            onClick = { 
                                                chosenCardId = card.id
                                                onInsert(card.cardId)
                                            },
                                            label = { androidx.compose.material3.Text(card.name) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PillMiuix(label: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.height(36.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick,
        onLongPress = onLongClick
    ) {
        Box(Modifier.fillMaxHeight().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            top.yukonga.miuix.kmp.basic.Text(
                label, 
                fontSize = 14.sp, 
                color = if (selected) Color.White else MiuixTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun KeyButtonMiuix(label: String, onClick: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.aspectRatio(1.5f),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            top.yukonga.miuix.kmp.basic.Text(label, fontSize = 28.sp)
        }
    }
}

@Composable
fun KeyButtonMaterial(label: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.aspectRatio(1.5f),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(label, fontSize = 28.sp)
        }
    }
}
