package org.cf0x.spicecompose.ui.screen.controllers

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.TimeSource
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.ButtonState
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsWrite
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import org.cf0x.spicecompose.ui.theme.SendMode

/**
 * Multi-touch button tracking engine — snapshot-based, dual-mode.
 *
 * Collects all currently-pressed pointers every frame, hit-tests against
 * every registered widget, diffs the result against the previous frame,
 * and sends exactly one [buttonsWrite] call per tick when anything changes.
 *
 * Two dispatch modes, configured via [CustomPreferences.sendMode]:
 * - **Event-driven**: [tick] fires from the [pointerInput] modifier when a
 *   [PointerEvent] arrives, throttled to [CustomPreferences.sendFrequency] Hz.
 * - **Crystal-driven**: [tick] fires from a fixed-frequency timer coroutine
 *   at [CustomPreferences.sendFrequency] Hz (50–1000).
 *
 * Usage in a Composable:
 * ```
 * val buttonControl = remember { ButtonControl(connectionManager) }
 * LaunchedEffect(Unit) { buttonControl.init() }
 *
 * Box(buttonControl.pointerInputModifier().fillMaxSize()) {
 *     ControllerButton(
 *         widget = buttonControl.registerWidget("BT-A"),
 *         buttonControl = buttonControl,
 *     )
 * }
 * ```
 */
class ButtonControl(private val connectionManager: ConnectionManager) {

    /** One entry per on-screen button widget; [isDown] updated every tick. */
    data class ButtonWidget(
        val name: String,
        var bounds: Rect = Rect.Zero,
        var isDown: Boolean = false,
    )

    /** Registry of on-screen button widgets. */
    val widgets = mutableStateListOf<ButtonWidget>()

    /** Button list from the last [init] call (parsed [buttonsRead] response). */
    val buttons = mutableStateListOf<ButtonState>()

    /** Incremented on every tick that changes state — drives UI recomposition. */
    val notifier = mutableIntStateOf(0)

    // ── Internal ─────────────────────────────────────────────────────────

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentPointers: Map<Long, Offset> = emptyMap()
    private var windowOffset = Offset.Zero
    private val pendingChanges = mutableListOf<ButtonState>()
    private var lastTickMark = TimeSource.Monotonic.markNow()

    // ── Public API ────────────────────────────────────────────────────────

    /** Read the current button list from the connected game and start the crystal ticker. */
    suspend fun init() {
        val client = connectionManager.getClient() ?: return
        val read = client.buttonsRead()
        read.forEach { it.active = false }
        buttons.clear()
        buttons.addAll(read)

        // Crystal ticker — always runs; gated by mode check inside the loop.
        scope.launch {
            while (isActive) {
                val freq = CustomPreferences.sendFrequency.coerceIn(50, 1000)
                delay(1000L / freq)
                if (CustomPreferences.sendMode == SendMode.CrystalDriven) {
                    tick()
                }
            }
        }
    }

    /** Register a new widget for the given button name (allows duplicates). */
    fun registerWidget(name: String): ButtonWidget {
        val w = ButtonWidget(name)
        widgets.add(w)
        return w
    }

    /** Update bounds for all widgets matching the given name. */
    fun updateBounds(name: String, bounds: Rect) {
        for (w in widgets) if (w.name == name) w.bounds = bounds
    }

    /** Clear all widget bounds — call on subview switch so stale widgets don't fire. */
    fun clearAllBounds() {
        for (w in widgets) w.bounds = Rect.Zero
    }

    // ── Pointer input modifier ────────────────────────────────────────────

    fun pointerInputModifier(): Modifier = Modifier
        .onGloballyPositioned { windowOffset = it.positionInWindow() }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    currentPointers = event.changes
                        .filter { it.pressed }
                        .associate { it.id.value to (windowOffset + it.position) }

                    if (CustomPreferences.sendMode == SendMode.EventDriven) {
                        val elapsed = lastTickMark.elapsedNow()
                        val intervalNs = 1_000_000_000L / CustomPreferences.sendFrequency.coerceIn(50, 1000)
                        if (elapsed.inWholeNanoseconds >= intervalNs) {
                            lastTickMark = TimeSource.Monotonic.markNow()
                            scope.launch { tick() }
                        }
                    }
                }
            }
        }

    // ── Tick ──────────────────────────────────────────────────────────────

    /** Diff snapshot against previous frame; flush changes when dirty. */
    private suspend fun tick() {
        pendingChanges.clear()
        var dirty = false
        var anyPress = false

        for (widget in widgets) {
            if (widget.bounds == Rect.Zero) continue
            val hit = currentPointers.values.any { widget.bounds.contains(it) }
            if (widget.isDown != hit) {
                widget.isDown = hit
                dirty = true
                if (hit) anyPress = true
                setState(widget.name, hit)
            }
        }

        if (dirty) {
            notifier.intValue++
            if (anyPress) maybeVibrate(30)

            val client = connectionManager.getClient()
            if (client != null && pendingChanges.isNotEmpty()) {
                try { client.buttonsWrite(pendingChanges.toList()) }
                catch (_: Exception) { /* best-effort */ }
            }
        }
    }

    // ── State helpers ─────────────────────────────────────────────────────

    private fun setState(name: String, pressed: Boolean) {
        val velocity = if (pressed) 1.0 else 0.0
        for (button in buttons) {
            if (button.name == name) {
                if (button.state != velocity) {
                    button.state = velocity
                    pendingChanges.add(button.copy())
                }
                break
            }
        }
    }
}
