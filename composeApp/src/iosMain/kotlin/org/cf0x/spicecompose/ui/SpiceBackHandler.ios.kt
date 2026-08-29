package org.cf0x.spicecompose.ui

import androidx.compose.runtime.Composable

/** No system back gesture is exposed to Compose on iOS. */
@Composable
actual fun SpiceBackHandler(enabled: Boolean, onBack: () -> Unit) {
}
