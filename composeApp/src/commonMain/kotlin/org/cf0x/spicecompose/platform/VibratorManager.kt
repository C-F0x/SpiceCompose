package org.cf0x.spicecompose.platform

import org.cf0x.spicecompose.ui.theme.ThemePreferences

expect object VibratorManager {
    fun vibrate(durationMillis: Long = 50)
}

fun maybeVibrate(durationMillis: Long = 50) {
    if (ThemePreferences.vibrationEnabled && vibrationAvailable) {
        VibratorManager.vibrate(durationMillis)
    }
}
