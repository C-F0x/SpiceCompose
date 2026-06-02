package org.cf0x.spicecompose.platform

actual object VibratorManager {
    actual fun vibrate(durationMillis: Long) {
        // Desktop doesn't typically have a vibrator
    }
}
