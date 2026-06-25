package org.cf0x.spicecompose.ui.screen.controllers.diy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

// ── DiyScreen (rewritten for v3 sidebar architecture) ──────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyScreen(connectionManager: ConnectionManager, onBack: () -> Unit) {
    val fullscreen = LocalFullscreenMode.current; val uiMode = LocalUiMode.current; val p = ThemePreferences
    val repo = remember { DiyRepository() }

    // Core state
    var editMode by remember { mutableStateOf(false) }
    var layout by remember { mutableStateOf<DiyLayout?>(null) }
    var selectedId by remember { mutableStateOf("") }
    var showGridToolbar by remember { mutableStateOf(false) }

    // Cross-component drag state (Sidebar Tab2/1 → Editor canvas)
    var dragWireBind by remember { mutableStateOf<String?>(null) }
    var dragWidgetType by remember { mutableStateOf<String?>(null) }

    // Sidebar position (anchor left, draggable)
    var sidebarOffset by remember { mutableStateOf(Offset(8f, 80f)) }
    var sidebarTab by remember { mutableIntStateOf(0) }

    // Drop feedback flash
    var boundFlashId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(boundFlashId) { if (boundFlashId != null) { delay(400); boundFlashId = null } }

    // ── CRUD helpers ───────────────────────────────────────────────────
    fun create(name: String, gm: String) { val l = DiyLayout(gameModel = gm, name = name); repo.save(l); layout = l }
    fun import(json: String) { try { val l = Json.decodeFromString<DiyLayout>(json); repo.save(l); layout = l } catch (_: Exception) {} }
    fun back() { layout = null; editMode = false; selectedId = "" }

    fun addWidget(w: DiyWidget) {
        val l = layout ?: return
        if (w.uni != null && l.widgets.any { it.uni == w.uni }) return
        val existing = l.widgets.map { it.name }
        val typeLabel = when (w) {
            is DiyWidget.Button -> "Button"; is DiyWidget.Fader -> "Fader"
            is DiyWidget.Knob -> "Knob"; is DiyWidget.Label -> "Label"
            is DiyWidget.Icon -> "Icon"; is DiyWidget.Grid -> "Grid"
            is DiyWidget.GuideLineWidget -> "Guide Line"; is DiyWidget.GuidePointWidget -> "Guide Point"
            is DiyWidget.GuideGridIndicator -> "Guide Grid"
            else -> "Widget"
        }
        val named = if (w.name == w.id) { // not yet named
            val n = nextName(typeLabel, existing)
            (when (w) {
                is DiyWidget.Button -> w.copy(name = n)
                is DiyWidget.Fader -> w.copy(name = n)
                is DiyWidget.Knob -> w.copy(name = n)
                is DiyWidget.Label -> w.copy(name = n)
                is DiyWidget.Icon -> w.copy(name = n)
                is DiyWidget.Grid -> w.copy(name = n)
                is DiyWidget.GuideLineWidget -> w.copy(name = n)
                is DiyWidget.GuidePointWidget -> w.copy(name = n)
                is DiyWidget.GuideGridIndicator -> w.copy(name = n)
            })
        } else w
        layout = l.copy(widgets = l.widgets + named)
    }

    fun moveWidget(id: String, x: Float, y: Float) {
        val l = layout ?: return
        layout = l.copy(widgets = l.widgets.map { when (it) {
            is DiyWidget.Button -> if (it.id == id) it.copy(x = x, y = y) else it
            is DiyWidget.Fader  -> if (it.id == id) it.copy(x = x, y = y) else it
            is DiyWidget.Knob   -> if (it.id == id) it.copy(x = x, y = y) else it
            is DiyWidget.Label  -> if (it.id == id) it.copy(x = x, y = y) else it
            is DiyWidget.Icon   -> if (it.id == id) it.copy(x = x, y = y) else it
            is DiyWidget.Grid   -> if (it.id == id) it.copy(x = x, y = y) else it
            is DiyWidget.GuideLineWidget -> if (it.id == id) it.copy(pos = if (it.orient == "h") y else x) else it
            is DiyWidget.GuidePointWidget -> if (it.id == id) it.copy(x = x, y = y) else it
            is DiyWidget.GuideGridIndicator -> it // no position
            else -> it
        } })
    }

    fun bindWidget(id: String, bind: String) {
        val l = layout ?: return
        val parts = id.split("_")
        val updated = if (parts.size >= 4 && parts[1].toIntOrNull() != null) {
            val gid = parts[0] + "_" + parts[1]; val r = parts[2].toInt()!!; val c = parts[3].toInt()!!
            l.copy(widgets = l.widgets.map { if (it is DiyWidget.Grid && it.id == gid) {
                val nc = it.cells.toMutableList(); nc.removeAll { x -> x.row == r && x.col == c }; nc.add(DiyWidget.GridCell(r, c, bind)); it.copy(cells = nc) } else it })
        } else l.copy(widgets = l.widgets.map { when {
            it.id == id && it is DiyWidget.Button -> it.copy(bind = bind)
            it.id == id && it is DiyWidget.Fader  -> it.copy(bind = bind)
            it.id == id && it is DiyWidget.Knob   -> it.copy(bind = bind)
            it.id == id && it is DiyWidget.Grid -> {
                val nc = it.cells.toMutableList()
                nc.add(DiyWidget.GridCell(0, 0, bind))
                it.copy(cells = nc)
            }
            else -> it } })
        layout = updated
    }

    fun deleteWidget(id: String) {
        val l = layout ?: return
        layout = l.copy(widgets = l.widgets.filter { it.id != id })
        if (selectedId == id) selectedId = ""
    }

    fun toggleWidgetEnabled(id: String) {
        val l = layout ?: return
        layout = l.copy(widgets = l.widgets.map {
            when (it) {
                is DiyWidget.Button -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.Fader -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.Knob -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.Label -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.Icon -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.Grid -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.GuideLineWidget -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.GuidePointWidget -> if (it.id == id) it.copy(enabled = !it.enabled) else it
                is DiyWidget.GuideGridIndicator -> if (it.id == id) it.copy(enabled = !it.enabled) else it
            }
        })
    }

    fun updateWidget(w: DiyWidget) {
        val l = layout ?: return
        // Sync GuideGridIndicator enabled → grid.enabled
        if (w is DiyWidget.GuideGridIndicator) {
            layout = l.copy(widgets = l.widgets.map { if (it.id == w.id) w else it },
                            grid = l.grid.copy(enabled = w.enabled))
        } else {
            layout = l.copy(widgets = l.widgets.map { if (it.id == w.id) w else it })
        }
    }

    fun reorderWidgets(from: Int, to: Int) {
        val l = layout ?: return
        val mutable = l.widgets.toMutableList()
        if (from < 0 || from >= mutable.size || to < 0 || to >= mutable.size) return
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        layout = l.copy(widgets = mutable)
    }

    fun addGuideLine(orient: String, pos: Float) {
        val id = uniqueId("gl")
        addWidget(DiyWidget.GuideLineWidget(id, orient, pos))
    }

    fun addGuidePoint(x: Float, y: Float) {
        val id = uniqueId("gp")
        addWidget(DiyWidget.GuidePointWidget(id, x, y))
    }

    fun updateGrid(gs: GridSettings) {
        layout = layout?.let { l -> l.copy(grid = gs) }
    }

    fun enableGuideGrid() {
        val l = layout ?: return
        layout = l.copy(grid = l.grid.copy(enabled = true))
    }

    fun unbindWidget(bindName: String) {
        val l = layout ?: return
        layout = l.copy(widgets = l.widgets.map { w ->
            when (w) {
                is DiyWidget.Button -> if (w.bind == bindName) w.copy(bind = "") else w
                is DiyWidget.Fader -> if (w.bind == bindName) w.copy(bind = "") else w
                is DiyWidget.Knob -> if (w.bind == bindName) w.copy(bind = "") else w
                is DiyWidget.Grid -> w.copy(cells = w.cells.map { if (it.bind == bindName) it.copy(bind = "") else it })
                else -> w
            }
        })
    }

    fun saveLayout() {
        val l = layout ?: return
        // Auto-renumber priorities 1,2,3... and persist
        val reordered = l.copy(widgets = l.widgets.mapIndexed { i, w -> w.withPriority(i + 1) })
        layout = reordered
        repo.save(reordered)
    }
    // Auto-save when switching sidebar tabs (skip initial load).
    var prevSidebarTab by remember { mutableIntStateOf(-1) }
    LaunchedEffect(sidebarTab) { if (prevSidebarTab >= 0 && sidebarTab >= 0) saveLayout(); prevSidebarTab = sidebarTab }
    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    // ── Top bar ────────────────────────────────────────────────────────
    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Scaffold(topBar = {
            if (!fullscreen.value && !p.toolbarHidden) SmallTopAppBar(
                title = layout?.name ?: "DIY Controller",
                navigationIcon = if (layout != null) {
                    { top.yukonga.miuix.kmp.basic.IconButton(onClick = { back() }) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } }
                } else {
                    { top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } }
                },
                actions = {
                    if (layout != null) {
                        if (editMode) {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = { saveLayout() }) {
                                top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Save, null)
                            }
                        }
                        top.yukonga.miuix.kmp.basic.IconButton(onClick = { editMode = !editMode; selectedId = "" }) {
                            top.yukonga.miuix.kmp.basic.Icon(if (editMode) Icons.Filled.PlayArrow else Icons.Filled.Edit, null)
                        }
                        if (editMode && selectedId.isNotEmpty()) {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = { deleteWidget(selectedId) }) {
                                top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Delete, null)
                            }
                        }
                    }
                    FullscreenAction()
                })
        }) { ip ->
            DiyBody(
                repo, layout, editMode, selectedId, connectionManager,
                if (fullscreen.value) PaddingValues(0.dp) else ip,
                showGridToolbar, dragWireBind, dragWidgetType, boundFlashId, { boundFlashId = it }, sidebarTab, { sidebarTab = it }, sidebarOffset,
                ::create, ::import, ::addWidget, ::moveWidget, ::bindWidget,
                ::updateWidget, ::deleteWidget, ::toggleWidgetEnabled, ::unbindWidget, ::enableGuideGrid, { showGridToolbar = !showGridToolbar }, ::reorderWidgets,
                ::addGuideLine, ::addGuidePoint, ::updateGrid,
                { showGridToolbar = it }, { selectedId = it },
                { dragWireBind = it }, { dragWidgetType = it },
                { sidebarOffset = it },
                { l -> layout = l },
                { l -> layout = l; editMode = false; selectedId = "" },
            )
        }
    } else {
        Scaffold(topBar = {
            if (!fullscreen.value && !p.toolbarHidden) TopAppBar(
                title = { Text(layout?.name ?: "DIY Controller") },
                navigationIcon = if (layout != null) {
                    { IconButton(onClick = { back() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
                } else {
                    { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
                },
                actions = {
                    if (layout != null) {
                        if (editMode) {
                            IconButton(onClick = { saveLayout() }) {
                                Icon(Icons.Rounded.Save, null)
                            }
                        }
                        IconButton(onClick = { editMode = !editMode; selectedId = "" }) {
                            Icon(if (editMode) Icons.Filled.PlayArrow else Icons.Filled.Edit, null)
                        }
                        if (editMode && selectedId.isNotEmpty()) {
                            IconButton(onClick = { deleteWidget(selectedId) }) {
                                Icon(Icons.Rounded.Delete, null)
                            }
                        }
                    }
                    FullscreenAction()
                })
        }) { ip ->
            DiyBody(
                repo, layout, editMode, selectedId, connectionManager,
                if (fullscreen.value) PaddingValues(0.dp) else ip,
                showGridToolbar, dragWireBind, dragWidgetType, boundFlashId, { boundFlashId = it }, sidebarTab, { sidebarTab = it }, sidebarOffset,
                ::create, ::import, ::addWidget, ::moveWidget, ::bindWidget,
                ::updateWidget, ::deleteWidget, ::toggleWidgetEnabled, ::unbindWidget, ::enableGuideGrid, { showGridToolbar = !showGridToolbar }, ::reorderWidgets,
                ::addGuideLine, ::addGuidePoint, ::updateGrid,
                { showGridToolbar = it }, { selectedId = it },
                { dragWireBind = it }, { dragWidgetType = it },
                { sidebarOffset = it },
                { l -> layout = l },
                { l -> layout = l; editMode = false; selectedId = "" },
            )
        }
    }
}

// ── Body composable ────────────────────────────────────────────────────

@Composable
private fun DiyBody(
    repo: DiyRepository,
    layout: DiyLayout?,
    editMode: Boolean,
    selectedId: String,
    connectionManager: ConnectionManager,
    padding: PaddingValues,
    showGridToolbar: Boolean,
    dragWireBind: String?,
    dragWidgetType: String?,
    boundFlashId: String?,
    onBoundFlash: (String?) -> Unit,
    sidebarTab: Int,
    onSidebarTabChanged: (Int) -> Unit,
    sidebarOffset: Offset,
    onCreate: (String, String) -> Unit,
    onImport: (String) -> Unit,
    onAddWidget: (DiyWidget) -> Unit,
    onMoveWidget: (String, Float, Float) -> Unit,
    onBindWidget: (String, String) -> Unit,
    onUpdateWidget: (DiyWidget) -> Unit,
    onDeleteWidget: (String) -> Unit,
    onToggleEnabled: (String) -> Unit,
    onUnbind: (String) -> Unit,
    onEnableGuideGrid: () -> Unit,
    onToggleGridToolbar: () -> Unit,
    onReorderWidgets: (Int, Int) -> Unit,
    onAddGuideLine: (String, Float) -> Unit,
    onAddGuidePoint: (Float, Float) -> Unit,
    onUpdateGrid: (GridSettings) -> Unit,
    onShowGridToolbar: (Boolean) -> Unit,
    onSelectId: (String) -> Unit,
    onDragWireBind: (String?) -> Unit,
    onDragWidgetType: (String?) -> Unit,
    onSidebarMoved: (Offset) -> Unit,
    onUpdateLayout: (DiyLayout) -> Unit,
    onOpenLayout: (DiyLayout) -> Unit,
) {
    var showNewDialog by remember { mutableStateOf(false) }
    var dragPtrX by remember { mutableFloatStateOf(0f) }
    var dragPtrY by remember { mutableFloatStateOf(0f) }
    var wireStartX by remember { mutableFloatStateOf(0f) }
    var wireStartY by remember { mutableFloatStateOf(0f) }
    var prevDragBind by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    // Snapshot start position when drag begins — X from sidebar right edge, Y from finger
    if (dragWireBind != null && prevDragBind == null) {
        wireStartX = with(density) { sidebarOffset.x + 260.dp.toPx() }
        wireStartY = dragPtrY
    }
    prevDragBind = dragWireBind

    if (showNewDialog) DiyNewDialog({ showNewDialog = false }, onCreate, onImport)

    Column(Modifier.fillMaxSize().padding(padding)) {
        Box(Modifier.weight(1f)
            .pointerInput(dragWireBind, dragWidgetType) {
                if (dragWireBind != null || dragWidgetType != null) {
                    awaitPointerEventScope {
                        while (true) {
                            val e = awaitPointerEvent()
                            val ch = e.changes.firstOrNull() ?: continue
                            dragPtrX = ch.position.x; dragPtrY = ch.position.y
                        }
                    }
                }
            }
        ) {
            when {
                layout == null -> DiyLayoutList(repo, onOpenLayout, { showNewDialog = true },
                    { Json { prettyPrint = true }.encodeToString(it) }, { onImport(it) })

                editMode -> {
                    val canvasPanZoom = sidebarTab == 0 && selectedId.isEmpty()
                    val canvasWidgetMove = sidebarTab == 0 && selectedId.isNotEmpty()
                    key(sidebarTab, selectedId) {
                    DiyEditor(
                        layout = layout,
                        onWidgetMoved = onMoveWidget,
                        selectedId = selectedId,
                        dragWireBind = dragWireBind,
                        dragWidgetType = dragWidgetType,
                        dragPtrX = dragPtrX,
                        dragPtrY = dragPtrY,
                        wireStartX = wireStartX,
                        wireStartY = wireStartY,
                        justBoundId = boundFlashId,
                        canvasPanZoom = canvasPanZoom,
                        canvasWidgetMove = canvasWidgetMove,
                        onDropBind = { bindName, widgetId -> onBindWidget(widgetId, bindName); onDragWireBind(null); onBoundFlash(widgetId) },
                        onDropWidget = { type, x, y ->
                            val id = uniqueId(type)
                            val w = when (type) {
                                "button" -> DiyWidget.Button(id, x = x, y = y, w = 0.1f, h = 0.1f)
                                "fader"  -> DiyWidget.Fader(id, x = x, y = y, w = 0.4f, h = 0.06f)
                                "knob"   -> DiyWidget.Knob(id, x = x, y = y, radius = 0.06f)
                                "label"  -> DiyWidget.Label(id, text = "Label", x = x, y = y)
                                "icon"   -> DiyWidget.Icon(id, iconName = "Gamepad", x = x, y = y, size = 0.06f)
                                "grid"   -> DiyWidget.Grid(id, rows = 3, cols = 3, x = x, y = y, cellW = 0.08f, cellH = 0.08f)
                                else -> return@DiyEditor
                            }
                            onAddWidget(w); onDragWidgetType(null); onSelectId(id)
                        },
                        onCancelDrag = { onDragWireBind(null); onDragWidgetType(null) },
                    )
                    }

                    // Sidebar overlay (left-anchored, draggable)
                    if (layout != null) {
                        DiySidebar(
                            layout = layout,
                            selectedId = selectedId,
                            onSelect = onSelectId,
                            onDeleteWidget = onDeleteWidget,
                            onReorder = onReorderWidgets,
                            onUpdateWidget = onUpdateWidget,
                            onToggleEnabled = onToggleEnabled,
                            onAddWidget = onAddWidget,
                            gameModel = layout.gameModel,
                            onDragBindStart = { onDragWireBind(it) },
                            onCancelDrag = { onDragWireBind(null); onDragWidgetType(null) },
                            onUnbind = onUnbind,
                            onEnableGuideGrid = onEnableGuideGrid,
                            onTabChanged = onSidebarTabChanged,
                            onToggleGridToolbar = onToggleGridToolbar,
                            panelOffset = sidebarOffset,
                            onPanelMoved = onSidebarMoved,
                        )
                    }

                    // Grid toolbar (bottom overlay)
                    if (showGridToolbar) {
                        Box(Modifier.padding(8.dp)) {
                            DiyGridToolbar(
                                grid = layout.grid,
                                onGridChange = onUpdateGrid,
                                onAddGuideLine = onAddGuideLine,
                                onAddGuidePoint = onAddGuidePoint,
                                onDismiss = { onShowGridToolbar(false) },
                            )
                        }
                    }
                }

                else -> DiyRenderer(layout, connectionManager)
            }
        }
    }
}

