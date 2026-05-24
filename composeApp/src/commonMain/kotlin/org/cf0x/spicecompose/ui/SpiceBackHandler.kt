package org.cf0x.spicecompose.ui

import androidx.compose.runtime.Composable

/**
 * KMP wrapper for BackHandler.
 * - Android: delegates to androidx.activity.compose.BackHandler
 * - Desktop: no-op (no system back concept)
 */
@Composable
expect fun SpiceBackHandler(enabled: Boolean = true, onBack: () -> Unit)
