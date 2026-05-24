package org.cf0x.spicecompose.ui

import androidx.compose.runtime.Composable

@Composable
actual fun SpiceBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on desktop — no system back concept
}
