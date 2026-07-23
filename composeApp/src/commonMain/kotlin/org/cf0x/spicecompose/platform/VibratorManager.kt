package org.cf0x.spicecompose.platform

import org.cf0x.spicecompose.ui.theme.CustomPreferences

expect object VibratorManager {
    fun vibrate(durationMillis: Long = 50)
}

fun maybeVibrate(durationMillis: Long = 50) {
    if (CustomPreferences.vibrationEnabled && vibrationAvailable) {
        VibratorManager.vibrate(durationMillis)
    }
}
