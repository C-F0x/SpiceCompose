package org.cf0x.spicecompose.ui.util

import android.os.Build

actual fun isRenderEffectSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
