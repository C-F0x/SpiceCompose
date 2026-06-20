package org.cf0x.spicecompose.ui.screen.controllers

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.ConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.ButtonState
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsWrite
import org.cf0x.spicecompose.platform.maybeVibrate

/**
 * Multi-touch button tracking engine for virtual game controllers.
 *
 * Translates pointer (touch/mouse) events into SPICE button-write calls.
 * Mirrors the Flutter [ButtonControl] from SpiceCompanion.
 *
 * Usage in a Composable:
 * ```
 * val buttonControl = remember { ButtonControl(connectionManager) }
 * LaunchedEffect(Unit) { buttonControl.init() }
 *
 * Box(buttonControl.pointerInputModifier().fillMaxSize()) {
 *     // Each button registers itself via registerWidget
 *     ControllerButton(
 *         name = "BT-A",
 *         buttonControl = buttonControl,
 *         modifier = Modifier.onGloballyPositioned { coords ->
 *             buttonControl.updateBounds("BT-A", coords)
 *         }
 *     )
 * }
 * ```
 */
class ButtonControl(private val connectionManager: ConnectionManager) {

    /** One entry per on-screen button; bounds updated every recomposition. */
    data class ButtonWidget(
        val name: String,
        var bounds: Rect = Rect.Zero,
        /** Pointer IDs (finger / stylus) currently touching this widget. */
        val pointers: MutableSet<Long> = mutableSetOf()
    ) {
        val isDown: Boolean get() = pointers.isNotEmpty()
    }

    /** Registry of on-screen button widgets. */
    val widgets = mutableStateListOf<ButtonWidget>()

    /** State list from the last [init] call (parsed [buttonsRead] response). */
    val buttons = mutableStateListOf<ButtonState>()

    /** Incremented on every press / release so dependent UI can recompose. */
    val notifier = mutableIntStateOf(0)

    // ── flush control ──────────────────────────────────────────────────────
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var buttonsFlushed = true
    private var writeCounter = 0

    // ── public API ─────────────────────────────────────────────────────────

    /** Read the current button list from the connected game (async). */
    suspend fun init() {
        val client = connectionManager.getClient() ?: return
        val read = client.buttonsRead()
        read.forEach { it.active = false }
        buttons.clear()
        buttons.addAll(read)
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

    // ── pointer input modifier ────────────────────────────────────────────

    private var windowOffset = Offset.Zero

    /**
     * Returns a [Modifier] that should be placed on the layout container
     * that wraps all controller buttons. Uses window coordinates so that
     * centering offsets (aspect-ratio boxes, etc.) don't break hit-testing.
     */
    fun pointerInputModifier(): Modifier = Modifier
        .onGloballyPositioned { windowOffset = it.positionInWindow() }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    for (change in event.changes) {
                        val pointerId = change.id.value
                        val position = windowOffset + change.position
                        val type = event.type

                        when (type) {
                            PointerEventType.Press   -> processPointer(pointerId, position, down = true)
                            PointerEventType.Release -> processPointer(pointerId, position, down = false)
                            PointerEventType.Move    -> processPointerMove(pointerId, position)
                            else -> {}
                        }
                    }
                }
            }
        }

    // ── pointer dispatch ──────────────────────────────────────────────────

    private fun processPointer(pointerId: Long, position: Offset, down: Boolean) {
        for (widget in widgets) {
            if (widget.bounds == Rect.Zero) continue

            val hit = widget.bounds.contains(position)
            var dirty = false

            if (hit) {
                if (down) {
                    if (widget.pointers.add(pointerId)) dirty = true
                } else {
                    if (widget.pointers.remove(pointerId)) dirty = true
                }
            } else if (widget.pointers.contains(pointerId)) {
                if (widget.pointers.remove(pointerId)) dirty = true
            }

            if (dirty) {
                notifier.intValue++
                setState(widget.name, widget.isDown)
            }
        }
    }

    private fun processPointerMove(pointerId: Long, position: Offset) {
        for (widget in widgets) {
            if (widget.bounds == Rect.Zero) continue

            val hit = widget.bounds.contains(position)
            val wasTracked = widget.pointers.contains(pointerId)
            var dirty = false

            if (hit && !wasTracked) {
                if (widget.pointers.add(pointerId)) dirty = true
                notifier.intValue++
                setState(widget.name, widget.isDown)
            } else if (!hit && wasTracked) {
                if (widget.pointers.remove(pointerId)) dirty = true
                notifier.intValue++
                setState(widget.name, widget.isDown)
            }
        }
    }

    // ── state → network ───────────────────────────────────────────────────

    private fun setState(name: String, pressed: Boolean) {
        val velocity = if (pressed) 1.0 else 0.0
        if (setVelocity(name, velocity)) {
            maybeVibrate(30)
        }
    }

    private fun setVelocity(name: String, state: Double): Boolean {
        var flush = false
        for (button in buttons) {
            if (button.name == name) {
                if (button.state != state) {
                    button.state = state
                    button.active = true
                    buttonsFlushed = false
                    flush = true
                }
                break
            }
        }
        if (flush) flushState()
        return flush
    }

    private fun flushState() {
        val client = connectionManager.getClient() ?: return

        scope.launch {
            if (buttonsFlushed || buttons.isEmpty() || writeCounter > 0) return@launch

            val activeButtons = mutableListOf<ButtonState>()
            for (button in buttons) {
                if (button.active) {
                    button.active = false
                    activeButtons.add(button)
                }
            }
            buttonsFlushed = true
            writeCounter++

            try {
                client.buttonsWrite(activeButtons)
                if (!buttonsFlushed) flushState()
            } catch (_: Exception) {
                // best-effort
            } finally {
                writeCounter--
            }
        }
    }
}
