package org.cf0x.spicecompose.platform

/**
 * Game-mode optimisations: high refresh rate + sustained performance.
 * Only has effect on Android; Desktop / Wasm are no-ops.
 */
expect object GameOptimizations {
    fun enable()
    fun disable()
}
