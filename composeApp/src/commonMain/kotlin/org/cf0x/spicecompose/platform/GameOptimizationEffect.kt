package org.cf0x.spicecompose.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Composable wrapper: enables game optimisations on enter,
 * disables on leave. No-op outside Android.
 */
@Composable
expect fun GameOptimizationEffect()
