package org.cf0x.spicecompose.ui

import androidx.compose.runtime.Composable

/**
 * KMP wrapper for BackHandler.
 * - Android: delegates to androidx.activity.compose.BackHandler
 * - Desktop: maps the window-level Escape key to a back event
 * - iOS: maps the native edge-swipe gesture to a back event
 */
@Composable
expect fun SpiceBackHandler(enabled: Boolean = true, onBack: () -> Unit)
