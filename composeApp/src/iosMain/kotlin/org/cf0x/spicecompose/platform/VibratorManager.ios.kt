package org.cf0x.spicecompose.platform

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual object VibratorManager {
    private var generator: UIImpactFeedbackGenerator? = null

    actual fun vibrate(durationMillis: Long) {
        // UIKit feedback generators must run on the main thread.
        val g = generator ?: UIImpactFeedbackGenerator(
            style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
        ).also { generator = it }
        g.impactOccurred()
    }
}
