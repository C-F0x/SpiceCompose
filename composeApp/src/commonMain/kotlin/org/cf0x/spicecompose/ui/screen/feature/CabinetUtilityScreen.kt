package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.data.CardConfig
import org.cf0x.spicecompose.data.CardRepository
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.*
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.platform.NfcManager
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.platform.nfcAvailable
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize
import org.cf0x.spicecompose.ui.theme.LocalStatusColors
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun CabinetUtilityScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences
    val statusColors = LocalStatusColors.current
    val windowSize = LocalWindowSize.current
    val isLarge = windowSize != WindowSize.Compact

    // ── Card management state ────────────────────────────────────────────
    val repository = remember { CardRepository() }
    var cards by remember { mutableStateOf(repository.getCards()) }
    var chosenCardId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardConfig?>(null) }

    // ── Keypad state ─────────────────────────────────────────────────────
    var currentMode by remember { mutableIntStateOf(0) }
    val modeLabel = if (currentMode == 0) "P1" else "P2"

    // ── Coin state ───────────────────────────────────────────────────────
    var coinBlocker by remember { mutableStateOf<Boolean?>(null) }
    var coinAmount by remember { mutableIntStateOf(p.coinDefaultAmount) }
    val selectedCard = remember(chosenCardId, cards) { if (chosenCardId == "nfc" || chosenCardId == null) null else cards.find { it.id == chosenCardId } }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    // ── NFC listener ─────────────────────────────────────────────────────
    LaunchedEffect(connection, chosenCardId) {
        if (connection == null || !nfcAvailable) return@LaunchedEffect
        NfcManager.tagIdFlow.collect { id ->
            if (chosenCardId == "nfc") { connection.cardInsert(currentMode, id); maybeVibrate(100) }
        }
    }

    // ── Coin blocker poll ────────────────────────────────────────────────
    LaunchedEffect(connection) {
        while (isActive) {
            coinBlocker = try { connection?.coinBlockerGet() } catch (_: Exception) { null }
            delay(2000)
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────
    val onInsert: (String) -> Unit = { id ->
        scope.launch { connection?.cardInsert(currentMode, id); maybeVibrate(100) }
    }
    val onKeyClick: (String) -> Unit = { key ->
        maybeVibrate(50); scope.launch { connection?.keypadsWrite(currentMode, key) }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────
    val showDialog = showAddDialog || editingCard != null
    if (showDialog) {
        CardEditDialog(
            show = true, card = editingCard,
            onSave = { cfg -> repository.addCard(cfg); cards = repository.getCards(); showAddDialog = false; editingCard = null },
            onDelete = { editingCard?.let { repository.deleteCard(it.id); if (chosenCardId == it.id) chosenCardId = null; cards = repository.getCards() }; showAddDialog = false; editingCard = null },
            onCancel = { showAddDialog = false; editingCard = null }
        )
    }

    // ── Shared composables ───────────────────────────────────────────────

    @Composable
    fun CoinRowCard() {
        val statusText = when (coinBlocker) { true -> strings.coinBlocked; false -> strings.coinOpen; null -> strings.coinChecking }
        val color = when (coinBlocker) { true -> statusColors.warning; false -> statusColors.healthy; null -> statusColors.neutral }
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                MiuixText(strings.coins, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                // ── Stepper: - / NUM / + ──
                MiuixIconButton(onClick = { if (coinAmount > 1) { coinAmount--; p.updateCoinDefaultAmount(coinAmount) } }) {
                    MiuixIcon(Icons.Rounded.Remove, null)
                }
                MiuixText("$coinAmount", modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                MiuixIconButton(onClick = { if (coinAmount < 16) { coinAmount++; p.updateCoinDefaultAmount(coinAmount) } }) {
                    MiuixIcon(Icons.Rounded.Add, null)
                }
                // ── Insert ──
                Spacer(Modifier.width(8.dp))
                TextButton(text = strings.coinInsert, onClick = { scope.launch { connection?.coinInsert(coinAmount) } }, colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary())
                Spacer(Modifier.weight(1f))
                // ── Blocker status ──
                Row(Modifier.background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    MiuixIcon(Icons.Rounded.Money, null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    MiuixText(statusText, fontSize = 13.sp, color = color)
                }
            }
        }
    }

    @Composable
    fun KeypadCard(modifier: Modifier = Modifier) {
        val keys = listOf("7","8","9", "4","5","6", "1","2","3", "0","00",".")
        val selectedCard = cards.find { it.id == chosenCardId }
        Card(modifier = modifier.padding(bottom = 12.dp)) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                MiuixText("Keypad", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // rows 0-3: number keys
                    for (row in 0..3) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (col in 0..2) {
                                val key = keys[row * 3 + col]
                                KeyButtonMiuix(key, Modifier.weight(1f).aspectRatio(1.5f)) {
                                    when (key) {
                                        "00" -> onKeyClick("A")
                                        "."  -> onKeyClick("D")
                                        else -> onKeyClick(key)
                                    }
                                }
                            }
                        }
                    }
                    // row 4: P1/P2 toggle + card swipe
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            text = modeLabel,
                            onClick = { currentMode = (currentMode + 1) % 2 },
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                        )
                        TextButton(
                            text = strings.cardSwipe,
                            onClick = {
                                when (chosenCardId) {
                                    "nfc" -> { /* NFC handles itself */ }
                                    else -> selectedCard?.let { scope.launch { onInsert(it.cardId) } }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = chosenCardId != null,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ProcessCard() {
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Column(Modifier.padding(12.dp)) {
                MiuixText(strings.process, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(text = strings.restart, onClick = { scope.launch { connection?.controlRestart() } }, modifier = Modifier.weight(1f), enabled = connection != null)
                    TextButton(text = strings.shutdown, onClick = { scope.launch { connection?.controlShutdown() } }, modifier = Modifier.weight(1f), enabled = connection != null, colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary())
                    TextButton(text = strings.reboot, onClick = { scope.launch { connection?.controlReboot() } }, modifier = Modifier.weight(1f), enabled = connection != null)
                }
            }
        }
    }

    @Composable
    fun CardManagementBlock() {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MiuixText(strings.cardManagement, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                MiuixIconButton(onClick = { showAddDialog = true }) { MiuixIcon(Icons.Rounded.Add, null) }
            }
            if (!nfcAvailable) {
                Spacer(Modifier.height(4.dp))
                MiuixText(strings.noNfcSupport, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (nfcAvailable) {
                    PillMiuix(strings.nfcLabel, chosenCardId == "nfc", true, { chosenCardId = if (chosenCardId == "nfc") null else "nfc" }, {})
                }
                cards.forEach { card ->
                    PillMiuix(card.name, chosenCardId == card.id, true, {
                        chosenCardId = if (chosenCardId == card.id) null else card.id
                    }, { editingCard = card })
                }
            }
        }
    }

    // ── Render ────────────────────────────────────────────────────────────
    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            val scrollBehavior = MiuixScrollBehavior()
            MiuixScaffold(
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        SmallTopAppBar(
                            title = strings.cabinetUtility,
                            navigationIcon = { MiuixIconButton(onClick = onBack) { MiuixIcon(MiuixIcons.Back, null) } },
                            actions = { FullscreenAction() },
                            scrollBehavior = scrollBehavior,
                        )
                    }
                }
            ) { innerPadding ->
                val topPadding = if (fullscreen.value) 0.dp else innerPadding.calculateTopPadding()
                if (isLarge) {
                    // ── Wide: keypad left, rest right ──────────────────
                    Row(
                        Modifier.fillMaxSize().padding(top = topPadding).padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        KeypadCard(modifier = Modifier.weight(1f).fillMaxHeight())
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            CoinRowCard()
                            ProcessCard()
                            CardManagementBlock()
                        }
                    }
                } else {
                    // ── Narrow: keypad first, then rest ─────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight().scrollEndHaptic().overScrollVertical()
                            .nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = topPadding),
                    ) {
                        item { Spacer(Modifier.height(12.dp)) }
                        item { KeypadCard() }
                        item { CoinRowCard() }
                        item { ProcessCard() }
                        item { CardManagementBlock() }
                        item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
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
                            title = { androidx.compose.material3.Text(strings.cabinetUtility) },
                            navigationIcon = { androidx.compose.material3.IconButton(onClick = onBack) { androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                            actions = { FullscreenAction() }
                        )
                    }
                }
            ) { innerPadding ->
                val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
                if (isLarge) {
                    Row(Modifier.fillMaxSize().padding(pad).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        KeypadCardMaterial(currentMode, modeLabel, strings.keyDelete, chosenCardId != null, strings.cardSwipe, onKeyClick, { currentMode = (currentMode + 1) % 2 }, { selectedCard?.let { scope.launch { onInsert(it.cardId) } } }, Modifier.weight(1f))
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            CoinRowCardMaterial(coinBlocker, coinAmount, strings, statusColors, { if (coinAmount > 1) { coinAmount--; p.updateCoinDefaultAmount(coinAmount) } }, { if (coinAmount < 16) { coinAmount++; p.updateCoinDefaultAmount(coinAmount) } }, { scope.launch { connection?.coinInsert(coinAmount) } })
                            Spacer(Modifier.height(12.dp))
                            ProcessCardMaterial(strings, connection, scope)
                            Spacer(Modifier.height(24.dp))
                            CardManagementBlockMaterial(strings, nfcAvailable, showAddDialog, { showAddDialog = true }, cards, chosenCardId, { chosenCardId = if (chosenCardId == it) null else it }, { editingCard = it })
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
                        KeypadCardMaterial(currentMode, modeLabel, strings.keyDelete, chosenCardId != null, strings.cardSwipe, onKeyClick, { currentMode = (currentMode + 1) % 2 }, { selectedCard?.let { scope.launch { onInsert(it.cardId) } } })
                        Spacer(Modifier.height(12.dp))
                        CoinRowCardMaterial(coinBlocker, coinAmount, strings, statusColors, { if (coinAmount > 1) { coinAmount--; p.updateCoinDefaultAmount(coinAmount) } }, { if (coinAmount < 16) { coinAmount++; p.updateCoinDefaultAmount(coinAmount) } }, { scope.launch { connection?.coinInsert(coinAmount) } })
                        Spacer(Modifier.height(12.dp))
                        ProcessCardMaterial(strings, connection, scope)
                        Spacer(Modifier.height(24.dp))
                        CardManagementBlockMaterial(strings, nfcAvailable, showAddDialog, { showAddDialog = true }, cards, chosenCardId, { chosenCardId = if (chosenCardId == it) null else it }, { editingCard = it })
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ── Material helper composables ────────────────────────────────────────

@Composable
private fun CoinRowCardMaterial(blocked: Boolean?, coinAmount: Int, strings: org.cf0x.spicecompose.ui.i18n.AppStrings, statusColors: org.cf0x.spicecompose.ui.theme.StatusColors, onDecrement: () -> Unit, onIncrement: () -> Unit, onInsert: () -> Unit) {
    val statusText = when (blocked) { true -> strings.coinBlocked; false -> strings.coinOpen; null -> strings.coinChecking }
    val color = when (blocked) { true -> statusColors.warning; false -> statusColors.healthy; null -> statusColors.neutral }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Text(strings.coins, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.IconButton(onClick = onDecrement) { androidx.compose.material3.Icon(Icons.Rounded.Remove, null) }
            androidx.compose.material3.Text("$coinAmount", modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
            androidx.compose.material3.IconButton(onClick = onIncrement) { androidx.compose.material3.Icon(Icons.Rounded.Add, null) }
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.TextButton(onClick = onInsert) { androidx.compose.material3.Text(strings.coinInsert) }
            Spacer(Modifier.weight(1f))
            Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(Icons.Rounded.Money, null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                androidx.compose.material3.Text(statusText, fontSize = 13.sp, color = color)
            }
        }
    }
}

@Composable
private fun KeypadCardMaterial(currentMode: Int, modeLabel: String, keyDelete: String, hasSelection: Boolean, cardSwipeLabel: String, onKeyClick: (String) -> Unit, onModeToggle: () -> Unit, onSwipeCard: () -> Unit, modifier: Modifier = Modifier) {
    val keys = listOf("7","8","9", "4","5","6", "1","2","3", "0","00",".")
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            androidx.compose.material3.Text("Keypad", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0..3) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in 0..2) {
                            val key = keys[row * 3 + col]
                            KeyButtonMaterial(key, Modifier.weight(1f).aspectRatio(1.5f)) {
                                when (key) { "00" -> onKeyClick("A"); "." -> onKeyClick("D"); else -> onKeyClick(key) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onModeToggle, modifier = Modifier.weight(1f)) { androidx.compose.material3.Text(modeLabel) }
                    Button(onClick = onSwipeCard, modifier = Modifier.weight(1f), enabled = hasSelection) { androidx.compose.material3.Text(cardSwipeLabel) }
                }
            }
        }
    }
}

@Composable
private fun ProcessCardMaterial(strings: org.cf0x.spicecompose.ui.i18n.AppStrings, connection: org.cf0x.spicecompose.network.SpiceClient?, scope: kotlinx.coroutines.CoroutineScope) {
    Card(modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            androidx.compose.material3.Text(strings.process, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { connection?.controlRestart() } }, modifier = Modifier.weight(1f), enabled = connection != null) { androidx.compose.material3.Text(strings.restart) }
                Button(onClick = { scope.launch { connection?.controlShutdown() } }, modifier = Modifier.weight(1f), enabled = connection != null, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { androidx.compose.material3.Text(strings.shutdown) }
                OutlinedButton(onClick = { scope.launch { connection?.controlReboot() } }, modifier = Modifier.weight(1f), enabled = connection != null) { androidx.compose.material3.Text(strings.reboot) }
            }
        }
    }
}

@Composable
private fun CardManagementBlockMaterial(strings: org.cf0x.spicecompose.ui.i18n.AppStrings, nfcAvailable: Boolean, showAddDialog: Boolean, onAddClick: () -> Unit, cards: List<CardConfig>, chosenCardId: String?, onSelect: (String) -> Unit, onEdit: (CardConfig) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Text(strings.cardManagement, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.IconButton(onClick = onAddClick) { androidx.compose.material3.Icon(Icons.Rounded.Add, null) }
        }
        if (!nfcAvailable) { androidx.compose.material3.Text(strings.noNfcSupport, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (nfcAvailable) { PillMaterial(strings.nfcLabel, chosenCardId == "nfc", true, { onSelect("nfc") }, {}) }
            cards.forEach { card -> PillMaterial(card.name, chosenCardId == card.id, true, { onSelect(card.id) }, { onEdit(card) }) }
        }
    }
}

// ── Key buttons ────────────────────────────────────────────────────────

@Composable
private fun KeyButtonMiuix(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier, onClick = onClick) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            MiuixText(label, fontSize = 24.sp)
        }
    }
}

@Composable
private fun KeyButtonMaterial(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(modifier = modifier, onClick = onClick) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(label, fontSize = 24.sp)
        }
    }
}

// ── Card pills ─────────────────────────────────────────────────────────

@Composable
private fun PillMiuix(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val bgColor = when { !enabled -> MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f); selected -> MiuixTheme.colorScheme.primary; else -> MiuixTheme.colorScheme.surfaceVariant }
    val txtColor = when { !enabled -> MiuixTheme.colorScheme.onSurface.copy(alpha = 0.3f); selected -> MiuixTheme.colorScheme.onPrimary; else -> MiuixTheme.colorScheme.onSurface }
    Card(modifier = Modifier.height(36.dp), colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = bgColor), onClick = { if (enabled) onClick() }, onLongPress = { if (enabled) onLongClick() }) {
        Box(Modifier.fillMaxHeight().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            MiuixText(label, fontSize = 14.sp, color = txtColor)
        }
    }
}

@Composable
private fun PillMaterial(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val bgColor = when { !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f); selected -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.surfaceVariant }
    val txtColor = when { !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f); selected -> MaterialTheme.colorScheme.onPrimary; else -> MaterialTheme.colorScheme.onSurface }
    Surface(modifier = Modifier.height(36.dp).combinedClickable(enabled = enabled, onClick = onClick, onLongClick = onLongClick), shape = MaterialTheme.shapes.medium, color = bgColor) {
        Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(label, fontSize = 14.sp, color = txtColor)
        }
    }
}
