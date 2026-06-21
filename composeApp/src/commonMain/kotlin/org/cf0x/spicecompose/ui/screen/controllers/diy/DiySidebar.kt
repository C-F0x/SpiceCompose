package org.cf0x.spicecompose.ui.screen.controllers.diy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ── Public composable ───────────────────────────────────────────────────

/**
 * Draggable 3-tab sidebar for the DIY editor.
 *
 * Tabs:
 *  - 0 **Widgets** — layer list with drag reorder, inline property editor
 *  - 1 **Library** — widget type palette, click to add
 *  - 2 **Backend** — browse buttons/analogs (hardcoded), drag to bind
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiySidebar(
    layout: DiyLayout,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDeleteWidget: (String) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onUpdateWidget: (DiyWidget) -> Unit,
    onToggleEnabled: (String) -> Unit,
    onAddWidget: (DiyWidget) -> Unit,
    gameModel: String,
    onDragBindStart: (bindName: String) -> Unit,
    onCancelDrag: () -> Unit,
    onUnbind: (bindName: String) -> Unit,
    onEnableGuideGrid: () -> Unit,
    onTabChanged: (Int) -> Unit,
    onToggleGridToolbar: () -> Unit,
    panelOffset: Offset,
    onPanelMoved: (Offset) -> Unit,
) {
    var panelX by remember { mutableFloatStateOf(panelOffset.x) }
    var panelY by remember { mutableFloatStateOf(panelOffset.y) }
    var dragPanel by remember { mutableStateOf(false) }
    var tabIndex by remember { mutableIntStateOf(0) }

    // Sync external offset changes
    if (!dragPanel) { panelX = panelOffset.x; panelY = panelOffset.y }

    // Cancel any active cross-drag when switching tabs
    LaunchedEffect(tabIndex) { onCancelDrag(); onSelect(""); onTabChanged(tabIndex) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .offset { IntOffset(panelX.roundToInt(), panelY.roundToInt()) }
            .width(260.dp)
            .fillMaxHeight(0.85f)
    ) {
        Column(Modifier.fillMaxSize()) {
            // ── Drag handle ─────────────────────────────────────────────
            Box(
                Modifier.fillMaxWidth().height(22.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .pointerInput("panelDrag") {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            dragPanel = true
                            panelX += dragAmount.x
                            panelY += dragAmount.y
                            onPanelMoved(Offset(panelX, panelY))
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.DragHandle, null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Tabs ────────────────────────────────────────────────────
            TabRow(selectedTabIndex = tabIndex) {
                Tab(tabIndex == 0, { tabIndex = 0 }) {
                    Icon(Icons.Rounded.Widgets, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Controls", fontSize = 11.sp)
                }
                Tab(tabIndex == 1, { tabIndex = 1 }) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Library", fontSize = 11.sp)
                }
                Tab(tabIndex == 2, { tabIndex = 2 }) {
                    Icon(Icons.Rounded.Link, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Backend", fontSize = 11.sp)
                }
            }

            // ── Tab body ────────────────────────────────────────────────
            when (tabIndex) {
                0 -> WidgetListTab(layout, selectedId, onSelect, onDeleteWidget, onReorder, onUpdateWidget, onToggleEnabled, onToggleGridToolbar)
                1 -> LibraryTab(onAddWidget)
                2 -> BackendTab(gameModel, onDragBindStart, onUnbind, layout)
            }
        }
    }
}

// ── Tab 0: Widget list (layers) + inline properties ─────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetListTab(
    layout: DiyLayout,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDeleteWidget: (String) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onUpdateWidget: (DiyWidget) -> Unit,
    onToggleEnabled: (String) -> Unit,
    onToggleGridToolbar: () -> Unit,
) {
    val widgets = layout.widgets
    val reorderable = widgets  // all widgets now, Grid is a regular library item

    Column(Modifier.fillMaxSize()) {
        // Quick-access Grid toolbar button
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onToggleGridToolbar, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.GridOn, null, Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val listState = rememberLazyListState()
        val hasSelection = selectedId.isNotEmpty()
        var draggedIdx by remember { mutableStateOf<Int?>(null) }
        var dragAccum by remember { mutableFloatStateOf(0f) }
        val itemH = 44.dp
        val density = LocalDensity.current
        val thresholdPx = with(density) { itemH.toPx() }

        LazyColumn(state = listState, modifier = Modifier.weight(1f), userScrollEnabled = !hasSelection && draggedIdx == null) {
            itemsIndexed(reorderable) { idx, w ->
                val isDragging = draggedIdx == idx
                WidgetRow(
                    widget = w, isSelected = w.id == selectedId, isLocked = false,
                    onClick = {
                        if (selectedId == w.id) onSelect("") else onSelect(w.id)
                    },
                    onDelete = { onDeleteWidget(w.id) },
                    onToggleEnabled = { onToggleEnabled(w.id) },
                    onDragStart = { draggedIdx = idx; dragAccum = 0f },
                    onDragMove = { dy ->
                        dragAccum += dy
                        val cur = draggedIdx ?: return@WidgetRow
                        while (dragAccum > thresholdPx && cur > 0) {
                            onReorder(cur, cur - 1)
                            dragAccum -= thresholdPx
                            draggedIdx = cur - 1
                        }
                        while (dragAccum < -thresholdPx && cur < reorderable.size - 1) {
                            onReorder(cur, cur + 1)
                            dragAccum += thresholdPx
                            draggedIdx = cur + 1
                        }
                    },
                    onDragEnd = { draggedIdx = null; dragAccum = 0f },
                )
            }
        }

        // Inline property editor for selected widget — larger area
        val sel = widgets.find { it.id == selectedId }
        if (sel != null) {
            InlinePropertyEditor(sel, widgets.map { it.name }, onUpdateWidget, onDismiss = { onSelect("") })
        }
    }
}

@Composable
private fun WidgetRow(
    widget: DiyWidget,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    onToggleEnabled: (() -> Unit)? = null,
    onDragStart: (() -> Unit)? = null,
    onDragMove: ((Float) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
) {
    val icon = when (widget) {
        is DiyWidget.Button -> "⬜"
        is DiyWidget.Fader -> "▬"
        is DiyWidget.Knob -> "◯"
        is DiyWidget.Label -> "T"
        is DiyWidget.Icon -> "✦"
        is DiyWidget.Grid -> "⊞"
        is DiyWidget.GuideLineWidget -> if (widget.orient == "h") "━" else "┃"
        is DiyWidget.GuidePointWidget -> "✚"
        is DiyWidget.GuideGridIndicator -> "┼"
    }
    val label = when (widget) {
        is DiyWidget.Label -> widget.text.ifEmpty { widget.name }
        else -> widget.name
    }
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    else Color.Transparent

    Row(
        Modifier.fillMaxWidth().background(bg).clickable { onClick() }.padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Drag handle (only for reorderable items)
        if (!isLocked && onDragStart != null) {
            Icon(
                Icons.Rounded.DragHandle, null, Modifier.size(18.dp)
                    .pointerInput(widget.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDrag = { _, dragAmount -> onDragMove?.invoke(dragAmount.y) },
                            onDragEnd = { onDragEnd?.invoke() },
                            onDragCancel = { onDragEnd?.invoke() },
                        )
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(Icons.Rounded.Lock, null, Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(6.dp))
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, Modifier.weight(1f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        // Enable/disable toggle
        if (onToggleEnabled != null) {
            IconButton(onClick = onToggleEnabled, modifier = Modifier.size(28.dp)) {
                Icon(
                    if (widget.enabled) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    null, Modifier.size(18.dp),
                    tint = if (widget.enabled) MaterialTheme.colorScheme.onSurfaceVariant
                           else MaterialTheme.colorScheme.error
                )
            }
        }
        // Delete button
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.Close, null, Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ── Inline property editor ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InlinePropertyEditor(
    widget: DiyWidget,
    existingNames: List<String>,
    onUpdate: (DiyWidget) -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    var name by remember(widget.id) { mutableStateOf(widget.name) }
    var nameError by remember { mutableStateOf("") }

    fun commitName(newName: String) {
        if (newName.isBlank()) { nameError = "Name required"; return }
        if (newName != widget.name && newName in existingNames) { nameError = "Name already used"; return }
        nameError = ""
        when (widget) {
            is DiyWidget.Button -> onUpdate(widget.copy(name = newName))
            is DiyWidget.Fader -> onUpdate(widget.copy(name = newName))
            is DiyWidget.Knob -> onUpdate(widget.copy(name = newName))
            is DiyWidget.Label -> onUpdate(widget.copy(name = newName))
            is DiyWidget.Icon -> onUpdate(widget.copy(name = newName))
            is DiyWidget.Grid -> onUpdate(widget.copy(name = newName))
            is DiyWidget.GuideLineWidget -> onUpdate(widget.copy(name = newName))
            is DiyWidget.GuidePointWidget -> onUpdate(widget.copy(name = newName))
            is DiyWidget.GuideGridIndicator -> onUpdate(widget.copy(name = newName))
        }
    }

    Column(
        Modifier.fillMaxWidth().height(320.dp).verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; commitName(it) },
            label = { Text("Name", fontSize = 11.sp) },
            isError = nameError.isNotEmpty(),
            supportingText = if (nameError.isNotEmpty()) {{ Text(nameError, fontSize = 10.sp) }} else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        // Commit on focus loss via a simple approach — just commit on every change
        // (the parent handles dedup via onUpdateWidget)

        when (widget) {
            is DiyWidget.Button -> {
                SecLabel("Position (%)"); PctSlider("X", widget.x) { onUpdate(widget.copy(x = it)) }; PctSlider("Y", widget.y) { onUpdate(widget.copy(y = it)) }
                var aspectLock by remember { mutableStateOf(false) }
                if (aspectLock) {
                    SecLabel("Size (1:1)"); SizeSlider("W/H", widget.w) { onUpdate(widget.copy(w = it, h = it)) }
                } else {
                    SecLabel("Size"); SizeSlider("W", widget.w) { onUpdate(widget.copy(w = it)) }; SizeSlider("H", widget.h) { onUpdate(widget.copy(h = it)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Aspect 1:1", Modifier.weight(1f), fontSize = 10.sp)
                    Switch(checked = aspectLock, onCheckedChange = { aspectLock = it; if (it) onUpdate(widget.copy(w = (widget.w + widget.h) / 2f, h = (widget.w + widget.h) / 2f)) })
                }
                SecLabel("Corner Radius")
                Slider(widget.cornerRadius, { onUpdate(widget.copy(cornerRadius = it)) }, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
                RotSlider(widget.rotation) { onUpdate(widget.copy(rotation = it)) }
            }
            is DiyWidget.Fader -> {
                PctSlider("X", widget.x) { onUpdate(widget.copy(x = it)) }; PctSlider("Y", widget.y) { onUpdate(widget.copy(y = it)) }
                SizeSlider("W", widget.w) { onUpdate(widget.copy(w = it)) }; SizeSlider("H", widget.h) { onUpdate(widget.copy(h = it)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-return", Modifier.weight(1f), fontSize = 10.sp)
                    Switch(checked = widget.autoReturn, onCheckedChange = { onUpdate(widget.copy(autoReturn = it)) })
                }
                SecLabel("Style")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thin", color = if (widget.style == "thin") MaterialTheme.colorScheme.primary else Color.Unspecified,
                        modifier = Modifier.clickable { onUpdate(widget.copy(style = "thin")) }.padding(4.dp), fontSize = 10.sp)
                    Text("|", Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                    Text("Full", color = if (widget.style == "full") MaterialTheme.colorScheme.primary else Color.Unspecified,
                        modifier = Modifier.clickable { onUpdate(widget.copy(style = "full")) }.padding(4.dp), fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Colorize", Modifier.weight(1f), fontSize = 10.sp)
                    Switch(checked = widget.colorize, onCheckedChange = { onUpdate(widget.copy(colorize = it)) })
                }
            }
            is DiyWidget.Knob -> {
                PctSlider("X", widget.x) { onUpdate(widget.copy(x = it)) }; PctSlider("Y", widget.y) { onUpdate(widget.copy(y = it)) }
                SizeSlider("R", widget.radius) { onUpdate(widget.copy(radius = it)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-return", Modifier.weight(1f), fontSize = 10.sp)
                    Switch(checked = widget.autoReturn, onCheckedChange = { onUpdate(widget.copy(autoReturn = it)) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Show tick", Modifier.weight(1f), fontSize = 10.sp)
                    Switch(checked = widget.showTick, onCheckedChange = { onUpdate(widget.copy(showTick = it)) })
                }
            }
            is DiyWidget.Label -> {
                var txt by remember { mutableStateOf(widget.text) }
                OutlinedTextField(value = txt, onValueChange = { txt = it; onUpdate(widget.copy(text = it)) }, label = { Text("Text") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                PctSlider("X", widget.x) { onUpdate(widget.copy(x = it)) }; PctSlider("Y", widget.y) { onUpdate(widget.copy(y = it)) }
                RotSlider(widget.rotation) { onUpdate(widget.copy(rotation = it)) }
            }
            is DiyWidget.Icon -> {
                var exp by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = it }) {
                    OutlinedTextField(widget.iconName, {}, readOnly = true, label = { Text("Icon") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))
                    ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                        DiyIconRegistry.names.forEach { n -> DropdownMenuItem(text = { Text(n) }, onClick = { onUpdate(widget.copy(iconName = n)); exp = false }) }
                    }
                }
                PctSlider("X", widget.x) { onUpdate(widget.copy(x = it)) }; PctSlider("Y", widget.y) { onUpdate(widget.copy(y = it)) }
                SizeSlider("Size", widget.size) { onUpdate(widget.copy(size = it)) }; RotSlider(widget.rotation) { onUpdate(widget.copy(rotation = it)) }
            }
            is DiyWidget.Grid -> {
                PctSlider("X", widget.x) { onUpdate(widget.copy(x = it)) }; PctSlider("Y", widget.y) { onUpdate(widget.copy(y = it)) }
            }
            is DiyWidget.GuideLineWidget -> {
                PctSlider("Pos", widget.pos) { onUpdate(widget.copy(pos = it)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Orient", Modifier.weight(1f), fontSize = 10.sp)
                    var h by remember { mutableStateOf(widget.orient == "h") }
                    Switch(h, { h = it; onUpdate(widget.copy(orient = if (it) "h" else "v")) })
                }
            }
            is DiyWidget.GuidePointWidget -> {
                PctSlider("X", widget.x) { onUpdate(widget.copy(x = it)) }; PctSlider("Y", widget.y) { onUpdate(widget.copy(y = it)) }
            }
            is DiyWidget.GuideGridIndicator -> {} // toggled via enabled eye icon
        }
    }
}

// ── Tab 1: Widget library — click to add ────────────────────────────

@Composable
private fun LibraryTab(
    onAddWidget: (DiyWidget) -> Unit,
) {
    data class LibCard(val type: String, val icon: String, val label: String, val desc: String, val isUnique: Boolean = false)

    val cards = listOf(
        LibCard("button", "⬜", "Button", "On/off tap target"),
        LibCard("fader", "▬", "Fader", "Horizontal slider"),
        LibCard("knob", "◯", "Knob", "Rotary dial"),
        LibCard("label", "T", "Label", "Display text"),
        LibCard("icon", "✦", "Icon", "Material symbol"),
        LibCard("grid", "⊞", "Controller Grid", "N×N button matrix"),
        LibCard("guide_grid", "┼", "Guide Grid", "% grid overlay", isUnique = true),
        LibCard("guide_h", "━", "Guide Line (H)", "Horizontal % line"),
        LibCard("guide_v", "┃", "Guide Line (V)", "Vertical % line"),
        LibCard("guide_point", "✚", "Guide Point", "X/Y % crosshair"),
    )

    Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Click to add", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        cards.forEach { card ->
            val bg = MaterialTheme.colorScheme.surfaceContainerHighest
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(bg)
                    .combinedClickable(
                        onClick = {
                            if (card.type == "guide_grid") {
                                val w = DiyWidget.GuideGridIndicator(uniqueId("gg"))
                                onAddWidget(w); return@combinedClickable
                            }
                            val w = when (card.type) {
                                "button" -> DiyWidget.Button(uniqueId("btn"), x = 0.5f, y = 0.5f, w = 0.1f, h = 0.1f)
                                "fader" -> DiyWidget.Fader(uniqueId("fdr"), x = 0.5f, y = 0.5f, w = 0.4f, h = 0.06f)
                                "knob" -> DiyWidget.Knob(uniqueId("knb"), x = 0.5f, y = 0.5f, radius = 0.06f)
                                "label" -> DiyWidget.Label(uniqueId("lbl"), text = "Label", x = 0.5f, y = 0.5f)
                                "icon" -> DiyWidget.Icon(uniqueId("ico"), iconName = "Gamepad", x = 0.5f, y = 0.5f, size = 0.06f)
                                "grid" -> DiyWidget.Grid(uniqueId("grd"), rows = 3, cols = 3, x = 0.3f, y = 0.3f, cellW = 0.08f, cellH = 0.08f)
                                "guide_h" -> DiyWidget.GuideLineWidget(uniqueId("glh"), orient = "h", pos = 0.5f)
                                "guide_v" -> DiyWidget.GuideLineWidget(uniqueId("glv"), orient = "v", pos = 0.5f)
                                "guide_point" -> DiyWidget.GuidePointWidget(uniqueId("gpt"), x = 0.5f, y = 0.5f)
                                else -> return@combinedClickable
                            }
                            onAddWidget(w)
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(card.icon, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                    Text(card.label, fontSize = 12.sp)
                    Text(card.desc + if (card.isUnique) " (unique)" else "", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Tab 2: Backend (collapsible items with bind-status circles) ──────

@Composable
private fun BackendTab(
    gameModel: String,
    onDragBindStart: (bindName: String) -> Unit,
    onUnbind: (bindName: String) -> Unit,
    layout: DiyLayout,
) {
    val bindings = bindingsFor(gameModel)
    var filter by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(0) } // 0=Buttons, 1=Analogs
    var expandedName by remember { mutableStateOf<String?>(null) }

    if (bindings == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No bindings for\n\"$gameModel\"", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val allButtons = bindings.buttons.filter { filter.isEmpty() || it.contains(filter, ignoreCase = true) }
    val allAnalogs = bindings.analogs.filter { filter.isEmpty() || it.contains(filter, ignoreCase = true) }

    fun findBound(name: String): DiyWidget? {
        return layout.widgets.firstOrNull { w ->
            when (w) {
                is DiyWidget.Button -> w.bind == name
                is DiyWidget.Fader -> w.bind == name
                is DiyWidget.Knob -> w.bind == name
                is DiyWidget.Grid -> w.cells.any { it.bind == name }
                else -> false
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(value = filter, onValueChange = { filter = it },
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Rounded.Search, null, Modifier.size(16.dp)) },
            trailingIcon = if (filter.isNotEmpty()) {{ IconButton({ filter = "" }, Modifier.size(16.dp)) { Icon(Icons.Rounded.Close, null, Modifier.size(14.dp)) } }} else null,
            modifier = Modifier.fillMaxWidth().height(48.dp), singleLine = true)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            TextButton(onClick = { page = 0; expandedName = null }, modifier = Modifier.weight(1f)) {
                Text("Buttons (${allButtons.size})", fontSize = 11.sp,
                    color = if (page == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { page = 1; expandedName = null }, modifier = Modifier.weight(1f)) {
                Text("Analogs (${allAnalogs.size})", fontSize = 11.sp,
                    color = if (page == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))

        val list = if (page == 0) allButtons else allAnalogs
        val listState = rememberLazyListState()
        LazyColumn(Modifier.fillMaxSize(), state = listState, userScrollEnabled = expandedName == null) {
            items(list.size) { idx ->
                val name = list[idx]
                val isExpanded = expandedName == name
                val boundWidget = findBound(name)
                val isBound = boundWidget != null

                // Collapsed row — centered name, click to expand
                Surface(
                    onClick = {
                        expandedName = if (isExpanded) null else name
                        if (isExpanded) expandedName = null  // toggle off
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isExpanded) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
                ) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(name, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // Expanded detail
                if (isExpanded) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // Left: bind status
                            Text(
                                if (isBound) "→ ${boundWidget!!.id}" else "Unbound",
                                Modifier.weight(1f), fontSize = 11.sp,
                                color = if (isBound) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            // Right: circle
                            if (isBound) {
                                // Filled circle — long-press to unbind
                                Box(Modifier.size(18.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { onUnbind(name); expandedName = null },
                                    ))
                            } else {
                                // Hollow circle — long-press to start drag
                                Box(Modifier.size(18.dp).clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .pointerInput(name) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { onDragBindStart(name) },
                                            onDragEnd = {},
                                            onDragCancel = {},
                                            onDrag = { _, _ -> },
                                        )
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shared slider helpers ───────────────────────────────────────────────

@Composable private fun SecLabel(t: String) {
    Text(t, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun PctSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    var v by remember { mutableIntStateOf((value * 100f).toInt()) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ${v}%", Modifier.width(50.dp), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
        Slider(value = v.toFloat(), onValueChange = { v = it.toInt(); onChange(v / 100f) }, valueRange = 0f..100f, modifier = Modifier.weight(1f))
    }
}

@Composable private fun SizeSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    var v by remember { mutableIntStateOf((value * 100f).toInt()) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ${v}%", Modifier.width(50.dp), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
        Slider(value = v.toFloat(), onValueChange = { v = it.toInt(); onChange(v / 100f) }, valueRange = 1f..200f, modifier = Modifier.weight(1f))
    }
}

@Composable private fun RotSlider(rot: Float, onChange: (Float) -> Unit) {
    var d by remember { mutableIntStateOf(rot.toInt()) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Rot: ${d}°", Modifier.width(50.dp), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
        Slider(value = d.toFloat(), onValueChange = { d = it.toInt(); onChange(it) }, valueRange = 0f..360f, steps = 35, modifier = Modifier.weight(1f))
    }
}
