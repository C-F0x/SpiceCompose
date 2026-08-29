package org.cf0x.spicecompose.platform

actual object GameOptimizations {
    actual fun enable() {
        // iOS has no game-mode hook.
    }

    actual fun disable() {
    }
}
