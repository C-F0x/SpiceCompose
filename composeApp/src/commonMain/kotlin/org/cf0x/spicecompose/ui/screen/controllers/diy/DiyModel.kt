package org.cf0x.spicecompose.ui.screen.controllers.diy

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

// ── Unique ID generator ────────────────────────────────────────────────

private var nextId = Random.nextLong()
internal fun uniqueId(prefix: String = "w") = "${prefix}_${nextId++}"

/** Generate a collision-free display name, e.g. "Button 1", "Fader 3". */
internal fun nextName(prefix: String, existing: List<String>): String {
    val cap = prefix.replaceFirstChar { it.uppercase() }
    var n = 1
    while ("$cap $n" in existing) n++
    return "$cap $n"
}

// ── Widget types ────────────────────────────────────────────────────────

@Serializable
sealed class DiyWidget {
    abstract val id: String
    @Transient open val enabled: Boolean = true
    @Transient open val uni: String? = null
    @Transient open val name: String = id
    @Transient open val priority: Int = 0

    /** Get a copy of this widget with the given priority set. */
    fun withPriority(p: Int): DiyWidget = when (this) {
        is Button -> copy(priority = p)
        is Fader -> copy(priority = p)
        is Knob -> copy(priority = p)
        is Label -> copy(priority = p)
        is Icon -> copy(priority = p)
        is Grid -> copy(priority = p)
        is GuideLineWidget -> copy(priority = p)
        is GuidePointWidget -> copy(priority = p)
        is GuideGridIndicator -> copy(priority = p)
    }

    @Serializable
    data class Button(
        override val id: String,
        val bind: String = "",
        val x: Float, val y: Float,
        val w: Float, val h: Float,
        val cornerRadius: Float = 0f,
        val rotation: Float = 0f,
        val sides: Int = 4,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = false,
    ) : DiyWidget()

    @Serializable
    data class Fader(
        override val id: String,
        val bind: String = "",
        val x: Float, val y: Float,
        val w: Float, val h: Float,
        val autoReturn: Boolean = true,
        val style: String = "thin",
        val colorize: Boolean = false,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = false,
    ) : DiyWidget()

    @Serializable
    data class Knob(
        override val id: String,
        val bind: String = "",
        val x: Float, val y: Float,
        val radius: Float,
        val autoReturn: Boolean = true,
        val showTick: Boolean = true,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = false,
    ) : DiyWidget()

    @Serializable
    data class Label(
        override val id: String,
        val text: String = "",
        val x: Float, val y: Float,
        val rotation: Float = 0f,
        val fontSize: Float = 12f,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = false,
    ) : DiyWidget()

    @Serializable
    data class Icon(
        override val id: String,
        val iconName: String = "Gamepad",
        val x: Float, val y: Float,
        val size: Float,
        val rotation: Float = 0f,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = false,
    ) : DiyWidget()

    @Serializable
    data class Grid(
        override val id: String,
        val rows: Int, val cols: Int,
        val x: Float, val y: Float,
        val cellW: Float, val cellH: Float,
        val gap: Float = 0.01f,
        val cells: List<GridCell> = emptyList(),
        val cornerRadius: Float = 4f,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = true,
    ) : DiyWidget()

    @Serializable
    data class GridCell(
        val row: Int, val col: Int,
        val bind: String = "",
    )

    @Serializable
    data class GuideLineWidget(
        override val id: String,
        val orient: String,
        val pos: Float,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = true,
    ) : DiyWidget()

    @Serializable
    data class GuidePointWidget(
        override val id: String,
        val x: Float, val y: Float,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = true,
    ) : DiyWidget()

    @Serializable
    data class GuideGridIndicator(
        override val id: String,
        override val name: String = id,
        override val priority: Int = 0,
        override val enabled: Boolean = true,
        override val uni: String? = "guide_grid",
    ) : DiyWidget()
}

// ── Grid settings ───────────────────────────────────────────────────────

@Serializable
data class GridSettings(
    val enabled: Boolean = false,
    val xStep: Int = 10,
    val yStep: Int = 10,
    val snapToLine: Boolean = false,
    val snapToPoint: Boolean = false,
)

// ── Top-level layout ────────────────────────────────────────────────────

@Serializable
data class DiyLayout(
    val version: Int = 1,
    val gameModel: String = "",
    val name: String = "New Layout",
    val grid: GridSettings = GridSettings(),
    val widgets: List<DiyWidget> = emptyList(),
)
