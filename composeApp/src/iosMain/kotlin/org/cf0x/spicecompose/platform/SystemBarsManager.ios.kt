package org.cf0x.spicecompose.platform

actual object SystemBarsManager {
    actual fun setFullscreen(enabled: Boolean) {
        // Fullscreen flags are handled by the Xcode host controller.
    }
}
