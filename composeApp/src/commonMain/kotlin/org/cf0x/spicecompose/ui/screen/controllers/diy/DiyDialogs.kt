package org.cf0x.spicecompose.ui.screen.controllers.diy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ── Game model picker data ──────────────────────────────────────────────

data class GameModelEntry(val label: String, val code: String)

val gameModelOptions: List<GameModelEntry> = listOf(
    GameModelEntry("Any (Generic)", ""),
) + allBindings.map { (code, b) -> GameModelEntry("${b.label} ($code)", code) }.sortedBy { it.label }

// ── Layout list ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyLayoutList(
    repo: DiyRepository,
    onOpen: (DiyLayout) -> Unit,
    onNew: () -> Unit,
    onExport: (DiyLayout) -> Unit,
    onImport: (String) -> Unit,
) {
    val layouts = remember { mutableStateListOf<DiyLayout>().also { it.addAll(repo.getAll()) } }
    LaunchedEffect(true) { layouts.apply { clear(); addAll(repo.getAll()) } }
    var filterModel by remember { mutableStateOf("") } // "" = all

    val filtered = if (filterModel.isEmpty()) layouts.toList()
        else layouts.filter { it.gameModel == filterModel }

    if (layouts.isEmpty()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Icon(Icons.Rounded.Gamepad, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("No layouts yet", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNew) {
                Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(4.dp)); Text("Create New")
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Saved Layouts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNew) { Icon(Icons.Rounded.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(2.dp)); Text("New") }
                }
                // Game model filter
                var filterExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = filterExpanded, onExpandedChange = { filterExpanded = it }) {
                    OutlinedTextField(
                        value = if (filterModel.isEmpty()) "All Models" else gameModelOptions.find { it.code == filterModel }?.label ?: filterModel,
                        onValueChange = {}, readOnly = true,
                        label = { Text("Filter") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(filterExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).padding(horizontal = 8.dp),
                    )
                    ExposedDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                        DropdownMenuItem(text = { Text("All Models") }, onClick = { filterModel = ""; filterExpanded = false })
                        gameModelOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt.label) }, onClick = { filterModel = opt.code; filterExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            items(filtered) { layout ->
                var showEdit by remember { mutableStateOf(false) }
                ListItem(
                    modifier = Modifier.combinedClickable(
                        onClick = { onOpen(layout) },
                        onLongClick = { showEdit = true },
                    ),
                    headlineContent = { Text(layout.name) },
                    supportingContent = { Text(layout.gameModel.ifEmpty { "Generic" } + " — ${layout.widgets.size} widgets") },
                    leadingContent = { Icon(Icons.Rounded.Gamepad, null, tint = MaterialTheme.colorScheme.primary) },
                )
                if (showEdit) {
                    DiyEditDialog(
                        layout = layout,
                        repo = repo,
                        onDismiss = { showEdit = false },
                        onExport = { onExport(layout) },
                    )
                }
            }
        }
    }
}

// ── New layout dialog ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyNewDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    onImport: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var gameExpanded by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf(gameModelOptions.first()) }
    var importJson by remember { mutableStateOf("") }
    var showImport by remember { mutableStateOf(false) }

    if (showImport) {
        AlertDialog(
            onDismissRequest = { showImport = false },
            title = { Text("Import JSON") },
            text = {
                OutlinedTextField(
                    value = importJson, onValueChange = { importJson = it },
                    label = { Text("Paste JSON here") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (importJson.isNotBlank()) { onImport(importJson); showImport = false; onDismiss() }
                }) { Text("Import") }
            },
            dismissButton = { TextButton(onClick = { showImport = false }) { Text("Cancel") } },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("New Layout") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Layout Name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ExposedDropdownMenuBox(
                        expanded = gameExpanded, onExpandedChange = { gameExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedGame.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Game") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(gameExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(expanded = gameExpanded, onDismissRequest = { gameExpanded = false }) {
                            gameModelOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.label) },
                                    onClick = { selectedGame = opt; gameExpanded = false },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { showImport = true }) { Icon(Icons.Rounded.FileOpen, null, Modifier.size(18.dp)); Spacer(Modifier.width(2.dp)); Text("Import") }
                    TextButton(onClick = {
                        if (name.isNotBlank()) { onCreate(name, selectedGame.code); onDismiss() }
                    }) { Text("Create") }
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        )
    }
}

// ── Edit dialog (long-press on existing layout) ─────────────────────────

@Composable
fun DiyEditDialog(
    layout: DiyLayout,
    repo: DiyRepository,
    onDismiss: () -> Unit,
    onExport: (DiyLayout) -> Unit,
) {
    val json = remember { Json { prettyPrint = true }.encodeToString(layout) }
    var showExport by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    if (showExport) {
        AlertDialog(
            onDismissRequest = { showExport = false },
            title = { Text("Export ${layout.name}") },
            text = {
                OutlinedTextField(
                    value = json, onValueChange = {},
                    readOnly = true, label = { Text("JSON") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            },
            confirmButton = { TextButton(onClick = { showExport = false }) { Text("Close") } },
        )
    } else if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${layout.name}?") },
            text = { Text("This cannot be undone.") },
            confirmButton = { TextButton(onClick = { repo.delete(layout.name); onDismiss() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(layout.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Game: ${layout.gameModel.ifEmpty { "Generic" }}")
                    Text("Widgets: ${layout.widgets.size}")
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { confirmDelete = true }) { Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp)); Spacer(Modifier.width(2.dp)); Text("Delete") }
                    TextButton(onClick = { showExport = true }) { Icon(Icons.Rounded.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.width(2.dp)); Text("Export") }
                    TextButton(onClick = { repo.save(layout); onDismiss() }) { Icon(Icons.Rounded.Save, null, Modifier.size(18.dp)); Spacer(Modifier.width(2.dp)); Text("Save") }
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        )
    }
}
