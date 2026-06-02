package org.cf0x.spicecompose.platform

expect object VibratorManager {
    fun vibrate(durationMillis: Long = 50)
}
