package org.cf0x.spicecompose.platform

import android.view.Display
import android.view.Window
import android.view.WindowManager

/**
 * On Android, locks the display to the highest available refresh rate
 * and enables sustained-performance mode (thermal throttling bypass).
 *
 * These are removed by [disable] (reset to default).
 */
actual object GameOptimizations {
    private var savedRefreshRate: Float = -1f
    private var savedPerformanceMode: Boolean = false
    private var window: Window? = null

    actual fun enable() {
        // Obtain the activity window lazily via the application context.
        // The caller is responsible for providing the Window reference
        // before calling enable(). For simplicity, expose a setter.
    }

    /** Must be called with the game controller's parent Activity window. */
    fun enable(window: Window) {
        this.window = window
        savedPerformanceMode = (window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0

        // Sustained performance mode — prevents thermal throttling
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Android N+ sustained performance mode
            @Suppress("DEPRECATION")
            window.setSustainedPerformanceMode(true)
        } catch (_: Exception) { /* best-effort */ }

        // Max refresh rate
        try {
            val display = window.context.display ?: return
            val modes = display.supportedModes ?: return
            if (modes.isEmpty()) return
            val maxRate = modes.maxOf { it.refreshRate }
            savedRefreshRate = window.attributes.preferredRefreshRate
            val params = window.attributes
            params.preferredRefreshRate = maxRate
            window.attributes = params
        } catch (_: Exception) { /* best-effort */ }
    }

    actual fun disable() {
        val w = window ?: return
        try {
            @Suppress("DEPRECATION")
            w.setSustainedPerformanceMode(savedPerformanceMode)
            val params = w.attributes
            params.preferredRefreshRate = savedRefreshRate
            w.attributes = params
        } catch (_: Exception) { /* best-effort */ }
        window = null
    }
}
