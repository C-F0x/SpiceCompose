package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.data.CardConfig
import org.cf0x.spicecompose.data.CardRepository
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.cardInsert
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CardManagerScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val repository = remember { CardRepository() }
    var cards by remember { mutableStateOf(repository.getCards()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardConfig?>(null) }
    
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getConnection()
    val scope = rememberCoroutineScope()

    val onInsert: (CardConfig, Int) -> Unit = { card, unit ->
        scope.launch {
            connection?.cardInsert(unit, card.cardId)
        }
    }

    val onToggleActive: (CardConfig) -> Unit = { card ->
        repository.setActive(if (card.active) null else card.id)
        cards = repository.getCards()
    }

    val onDelete: (String) -> Unit = { id ->
        repository.deleteCard(id)
        cards = repository.getCards()
    }

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
                        title = strings.cardManager,
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                            }
                        },
                        actions = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = { showAddDialog = true }) {
                                top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Add, null)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                if (cards.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        top.yukonga.miuix.kmp.basic.Text(strings.noCards)
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(innerPadding)) {
                        items(cards, key = { it.id }) { card ->
                            CardItemMiuix(card, 
                                onClick = { onInsert(card, 0) }, 
                                onLongClick = { editingCard = card },
                                onActiveToggle = { onToggleActive(card) },
                                onDelete = { onDelete(card.id) }
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
                        title = { androidx.compose.material3.Text(strings.cardManager) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    androidx.compose.material3.FloatingActionButton(onClick = { showAddDialog = true }) {
                        androidx.compose.material3.Icon(Icons.Rounded.Add, null)
                    }
                }
            ) { innerPadding ->
                if (cards.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text(strings.noCards)
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(innerPadding)) {
                        items(cards, key = { it.id }) { card ->
                            CardItemMaterial(card, 
                                onClick = { onInsert(card, 0) },
                                onEdit = { editingCard = card },
                                onActiveToggle = { onToggleActive(card) },
                                onDelete = { onDelete(card.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardItemMiuix(card: CardConfig, onClick: () -> Unit, onLongClick: () -> Unit, onActiveToggle: () -> Unit, onDelete: () -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        onClick = onClick,
        onLongPress = { showOptions = true }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = Icons.Rounded.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (card.active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                top.yukonga.miuix.kmp.basic.Text(card.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                top.yukonga.miuix.kmp.basic.Text(card.cardId, fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantActions)
            }
            if (card.active) {
                top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Check, null, tint = MiuixTheme.colorScheme.primary)
            }
        }
    }

    if (showOptions) {
        top.yukonga.miuix.kmp.overlay.OverlayDialog(
            show = showOptions,
            onDismissRequest = { showOptions = false },
            title = card.name,
            content = {
                Column {
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = strings.editCard,
                        onClick = { showOptions = false; onLongClick() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = if (card.active) "Deactivate" else "Set as Active",
                        onClick = { showOptions = false; onActiveToggle() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = strings.delete,
                        onClick = { showOptions = false; onDelete() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        )
    }
}

@Composable
fun CardItemMaterial(card: CardConfig, onClick: () -> Unit, onEdit: () -> Unit, onActiveToggle: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { androidx.compose.material3.Text(card.name) },
        supportingContent = { androidx.compose.material3.Text(card.cardId) },
        leadingContent = { androidx.compose.material3.Icon(Icons.Rounded.CreditCard, null, tint = if (card.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = {
            Row {
                androidx.compose.material3.IconButton(onClick = onEdit) {
                    androidx.compose.material3.Icon(Icons.Default.Edit, null)
                }
                androidx.compose.material3.IconButton(onClick = onDelete) {
                    androidx.compose.material3.Icon(Icons.Default.Delete, null)
                }
            }
        }
    )
}
